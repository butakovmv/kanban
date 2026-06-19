CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    file_name VARCHAR(500) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_key VARCHAR(1000) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_project_id ON documents(project_id);
