CREATE TABLE IF NOT EXISTS recovery_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recovery_tokens_token_hash ON recovery_tokens(token_hash);
CREATE INDEX idx_recovery_tokens_user_id ON recovery_tokens(user_id);
