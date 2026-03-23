import { requestJson } from '../http/apiClient.js';

export function createApprovalConsoleRepository(client = requestJson) {
	return {
		fetchSession() {
			return client('/api/session', { allowAnonymous: true });
		},
		fetchDashboard() {
			return client('/api/dashboard');
		},
		fetchUsersByStatus(status) {
			return client(`/api/admin/users?status=${status}`);
		},
		fetchAdminNotifications(limit = 10) {
			return client(`/api/admin/notifications?limit=${limit}`);
		},
		checkLoginId(loginId) {
			return client(`/api/registration/login-id-check?loginId=${encodeURIComponent(loginId)}`);
		},
		completeRegistration(payload) {
			return client('/api/registration/complete', {
				method: 'POST',
				body: JSON.stringify(payload)
			});
		},
		withdrawAccount(reason) {
			return client('/api/account/withdraw', {
				method: 'POST',
				body: JSON.stringify({ reason })
			});
		},
		approveUser(userId, roles) {
			return client(`/api/admin/users/${userId}/approve`, {
				method: 'POST',
				body: JSON.stringify({ roles })
			});
		},
		rejectUser(userId) {
			return client(`/api/admin/users/${userId}/reject`, {
				method: 'POST'
			});
		}
	};
}