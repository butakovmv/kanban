CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY,
    project_id UUID,
    board_id UUID,
    document_id UUID,
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_project_id ON audit_log(project_id);
CREATE INDEX idx_audit_log_board_id ON audit_log(board_id);
CREATE INDEX idx_audit_log_document_id ON audit_log(document_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);
