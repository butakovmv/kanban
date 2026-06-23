DROP TABLE IF EXISTS documents;

CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    path VARCHAR(1000) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_project_id ON documents(project_id);
CREATE UNIQUE INDEX idx_documents_project_path_title ON documents(project_id, path, title);
