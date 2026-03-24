export const ASSIGNABLE_ROLES = ['ROLE_USER', 'ROLE_MANAGER', 'ROLE_ADMIN'];
export const STATUS_TABS = ['PENDING', 'ACTIVE', 'REJECTED', 'WITHDRAWN'];
export const DEFAULT_APPROVAL_ROLE = 'ROLE_USER';

export function createInitialState() {
	return {
		loading: true,
		busy: false,
		session: null,
		dashboard: null,
		users: [],
		notifications: [],
		roleSelections: {},
		selectedStatus: 'PENDING',
		registrationForm: createRegistrationForm(),
		loginIdCheck: null,
		withdrawReason: '',
		flash: '',
		error: ''
	};
}

export function createRegistrationForm(seed = {}) {
	return {
		loginId: seed.loginId ?? '',
		displayName: seed.displayName ?? '',
		contactNumber: seed.contactNumber ?? '',
		agreedToTerms: seed.agreedToTerms ?? false
	};
}

function suggestLoginIdFromClaims(claims = {}) {
	const email = String(claims.email ?? '').trim().toLowerCase();
	const localPart = email.includes('@') ? email.split('@')[0] : '';
	const normalized = localPart.replace(/[^a-z0-9_-]/g, '');
	if (normalized.length >= 4) {
		return normalized.slice(0, 20);
	}
	return '';
}

function suggestContactFromClaims(claims = {}) {
	return String(claims.mobile ?? claims.mobile_e164 ?? '').trim();
}

export function hydrateRegistrationForm(account, claims = {}, existingForm = createRegistrationForm()) {
	if (!account) {
		return {
			loginId: existingForm.loginId || suggestLoginIdFromClaims(claims),
			displayName: existingForm.displayName || String(claims.name ?? ''),
			contactNumber: existingForm.contactNumber || suggestContactFromClaims(claims),
			agreedToTerms: existingForm.agreedToTerms
		};
	}
	return {
		loginId: existingForm.loginId || account.loginId || suggestLoginIdFromClaims(claims),
		displayName: existingForm.displayName || account.displayName || '',
		contactNumber: existingForm.contactNumber || account.contactNumber || suggestContactFromClaims(claims),
		agreedToTerms: existingForm.agreedToTerms || Boolean(account.termsAgreedAt)
	};
}

export function isAuthenticated(session) {
	return Boolean(session?.authenticated);
}

export function hasAccount(session) {
	return Boolean(session?.account);
}

export function needsRegistration(session) {
	return Boolean(session?.account?.registrationRequired);
}

export function isWithdrawn(session) {
	return Boolean(session?.account?.withdrawn);
}

export function canOpenDashboard(session) {
	return Boolean(session?.account?.active);
}

export function canManageUsers(session) {
	return Boolean(session?.account?.admin);
}

export function canWithdraw(session) {
	return Boolean(session?.account?.canWithdraw);
}

export function updateRegistrationField(form, field, value) {
	return {
		...form,
		[field]: value
	};
}

export function normalizeRoles(roles) {
	const requested = Array.isArray(roles) ? roles : [];
	return Array.from(new Set(requested))
		.map((role) => String(role).trim().toUpperCase())
		.filter((role) => ASSIGNABLE_ROLES.includes(role));
}

export function hydrateRoleSelections(users, existingSelections = {}) {
	return users.reduce((nextSelections, user) => ({
		...nextSelections,
		[user.id]: resolveInitialRoles(user)
	}), { ...existingSelections });
}

export function toggleRoleSelection(roleSelections, userId, roleCode) {
	const currentRoles = roleSelections[userId] ?? [];
	const nextRoles = currentRoles.includes(roleCode)
		? currentRoles.filter((value) => value !== roleCode)
		: [...currentRoles, roleCode];

	return {
		...roleSelections,
		[userId]: normalizeRoles(nextRoles)
	};
}

function resolveInitialRoles(user) {
	const normalized = normalizeRoles(user.roles ?? []);
	if (normalized.length > 0) {
		return normalized;
	}
	return user.status === 'PENDING' ? [DEFAULT_APPROVAL_ROLE] : normalized;
}

export function statusTone(status) {
	switch (status) {
		case 'ACTIVE':
			return 'active';
		case 'REJECTED':
			return 'rejected';
		case 'WITHDRAWN':
			return 'withdrawn';
		default:
			return 'pending';
	}
}

export function loginIdCheckTone(result) {
	if (!result) {
		return 'neutral';
	}
	if (result.available) {
		return 'success';
	}
	return result.status === 'WITHDRAWN_MEMBER' ? 'warning' : 'error';
}

export function formatDate(value, locale = 'ko-KR') {
	if (!value) {
		return '-';
	}
	return new Date(value).toLocaleString(locale);
}