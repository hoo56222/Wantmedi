CREATE TABLE public.signup_verification_token (
	token_hash VARCHAR(64) PRIMARY KEY,
	email VARCHAR(254) NOT NULL,
	expires_at TIMESTAMPTZ NOT NULL,
	used_at TIMESTAMPTZ
);