-- Database initialization script for CloudNativePG

-- Create additional extensions that might be needed by DC apps
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Grant additional permissions to the application user
GRANT CREATE ON SCHEMA public TO ${DC_APP};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${DC_APP};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${DC_APP};

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DC_APP};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DC_APP};

-- Create a simple health check function
CREATE OR REPLACE FUNCTION health_check() RETURNS text AS $$
BEGIN
    RETURN 'OK';
END;
$$ LANGUAGE plpgsql;

-- Log initialization completion
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed for application: %', '${DC_APP}';
END $$;