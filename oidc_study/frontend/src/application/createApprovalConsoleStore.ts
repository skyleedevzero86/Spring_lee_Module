import { writable } from 'svelte/store';
import {
	canManageUsers,
	canOpenDashboard,
	createInitialState,
	hydrateRegistrationForm,
	hydrateRoleSelections,
	toggleRoleSelection,
	updateRegistrationField
} from '../domain/approvalConsole.js';
import { createApprovalConsoleRepository } from '../infrastructure/repository/approvalConsoleRepository.js';

export function createApprovalConsoleStore(repository = createApprovalConsoleRepository()) {
	const store = writable(createInitialState());
	let snapshot = createInitialState();
	store.subscribe((value) => {
		snapshot = value;
	});

	const setState = (reducer) => {
		store.update((current) => {
			const next = reducer(current);
			snapshot = next;
			return next;
		});
	};

	async function initialize() {
		await refresh({ selectedStatus: snapshot.selectedStatus, clearMessages: true });
	}

	async function selectStatus(status) {
		await refresh({ selectedStatus: status });
	}

	function toggleRole(userId, roleCode) {
		setState((current) => ({
			...current,
			roleSelections: toggleRoleSelection(current.roleSelections, userId, roleCode)
		}));
	}

	function updateRegistration(field, value) {
		setState((current) => ({
			...current,
			registrationForm: updateRegistrationField(current.registrationForm, field, value),
			loginIdCheck: field === 'loginId' ? null : current.loginIdCheck
		}));
	}

	function updateWithdrawReason(value) {
		setState((current) => ({
			...current,
			withdrawReason: value
		}));
	}

	async function checkLoginId() {
		setState((current) => ({ ...current, busy: true, error: '' }));
		try {
			const result = await repository.checkLoginId(snapshot.registrationForm.loginId);
			setState((current) => ({ ...current, busy: false, loginIdCheck: result }));
		} catch (error) {
			setState((current) => ({ ...current, busy: false, error: error.message }));
		}
	}

	async function submitRegistration() {
		await executeMutation(async () => {
			await repository.completeRegistration(snapshot.registrationForm);
			return '가입 신청이 접수되었습니다. 관리자 승인을 기다려 주세요.';
		});
	}

	async function withdrawAccount() {
		await executeMutation(async () => {
			await repository.withdrawAccount(snapshot.withdrawReason);
			return '탈퇴 처리되었습니다. 이후 동일 아이디 사용은 관리자 문의가 필요합니다.';
		});
		await refresh({ selectedStatus: snapshot.selectedStatus, flash: snapshot.flash, clearMessages: false });
	}

	async function approveUser(userId) {
		const roles = snapshot.roleSelections[userId] ?? [];
		if (roles.length === 0) {
			setState((current) => ({
				...current,
				error: '권한은 최소 1개 이상 선택해야 승인할 수 있습니다.',
				flash: ''
			}));
			return;
		}

		await executeMutation(async () => {
			await repository.approveUser(userId, roles);
			return '사용자 승인과 권한 부여가 완료되었습니다.';
		});
	}

	async function rejectUser(userId) {
		await executeMutation(async () => {
			await repository.rejectUser(userId);
			return '사용자 상태를 반려로 변경했습니다.';
		});
	}

	async function executeMutation(work) {
		setState((current) => ({ ...current, busy: true, flash: '', error: '' }));
		try {
			const flash = await work();
			await refresh({ selectedStatus: snapshot.selectedStatus, flash });
		} catch (error) {
			setState((current) => ({ ...current, busy: false, error: error.message }));
		}
	}

	async function refresh({ selectedStatus, flash = '', clearMessages = false } = {}) {
		setState((current) => ({
			...current,
			loading: true,
			selectedStatus: selectedStatus ?? current.selectedStatus,
			flash: clearMessages ? '' : flash,
			error: ''
		}));

		try {
			const session = await repository.fetchSession();
			const dashboard = canOpenDashboard(session) ? await repository.fetchDashboard() : null;
			const users = canManageUsers(session)
				? await repository.fetchUsersByStatus(selectedStatus ?? snapshot.selectedStatus)
				: [];
			const notifications = canManageUsers(session)
				? await repository.fetchAdminNotifications()
				: [];

			setState((current) => ({
				...current,
				loading: false,
				busy: false,
				session,
				dashboard,
				users,
				notifications,
				roleSelections: hydrateRoleSelections(users, current.roleSelections),
				registrationForm: hydrateRegistrationForm(
					session.account,
					session.oidcClaims ?? {},
					current.registrationForm
				),
				selectedStatus: selectedStatus ?? current.selectedStatus,
				flash: clearMessages ? '' : flash,
				error: ''
			}));
		} catch (error) {
			setState((current) => ({
				...current,
				loading: false,
				busy: false,
				error: error.message,
				flash: ''
			}));
		}
	}

	return {
		subscribe: store.subscribe,
		initialize,
		selectStatus,
		toggleRole,
		updateRegistration,
		updateWithdrawReason,
		checkLoginId,
		submitRegistration,
		withdrawAccount,
		approveUser,
		rejectUser
	};
}