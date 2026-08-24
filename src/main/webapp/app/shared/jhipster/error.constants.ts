export const PROBLEM_BASE_URL = 'https://www.jhipster.tech/problem';
export const EMAIL_ALREADY_USED_TYPE = `${PROBLEM_BASE_URL}/email-already-used`;
export const LOGIN_ALREADY_USED_TYPE = `${PROBLEM_BASE_URL}/login-already-used`;
export const INVALID_PASSWORD_TYPE = `${PROBLEM_BASE_URL}/invalid-password`;
// SEC-04: the server refuses everything but the account's own profile and the change-password
// endpoint until a pre-policy password is replaced. Distinct from a plain 403 so the client can send
// the user somewhere useful instead of showing a dead end.
export const PASSWORD_CHANGE_REQUIRED_TYPE = `${PROBLEM_BASE_URL}/password-change-required`;
