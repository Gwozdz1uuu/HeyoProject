# Database Migrations

This project uses **Flyway** for database version control and migrations.

## Directory Structure

```
db/migration/
├── V1__initial_schema.sql    # Initial database schema
├── V2__seed_data.sql         # Sample data for development
└── README.md                 # This file
```

## Migration Naming Convention

Flyway uses a specific naming convention for migration files:

```
V{version}__{description}.sql
```

- `V` - Prefix indicating a versioned migration
- `{version}` - Version number (e.g., 1, 2, 3 or 1.1, 1.2)
- `__` - Double underscore separator
- `{description}` - Description with underscores instead of spaces
- `.sql` - SQL file extension

### Examples:
- `V1__initial_schema.sql`
- `V2__add_user_status.sql`
- `V3__create_audit_table.sql`

## How Migrations Work

1. **On application startup**, Flyway automatically:
   - Creates `flyway_schema_history` table (if not exists)
   - Scans `db/migration` folder for migration files
   - Executes any new migrations in version order
   - Records executed migrations in history table

2. **Migrations are immutable** - Once a migration has been applied to any environment, **do not modify it**. Create a new migration instead.

## Creating New Migrations

1. Create a new SQL file following the naming convention
2. Increment the version number
3. Write your SQL statements
4. Test locally before committing

### Example: Adding a new column

```sql
-- V3__add_user_status_column.sql
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
CREATE INDEX idx_users_status ON users(status);
```

## Running Migrations

### Automatic (Default)
Migrations run automatically when the Spring Boot application starts.

### Manual via Maven
```bash
# Run migrations
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Validate migrations
./mvnw flyway:validate

# Clean database (DANGER: drops all objects!)
./mvnw flyway:clean
```

## Configuration

Flyway is configured in `application.properties`:

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
```

## Production Considerations

1. **Remove seed data migration** (`V2__seed_data.sql`) in production, or use Flyway profiles to skip it.

2. **Backup before migrations** - Always backup your database before running migrations in production.

3. **Test migrations** - Run migrations on a copy of production data before deploying.

4. **Use transactions** - MySQL InnoDB supports transactional DDL for some operations.

## Troubleshooting

### Migration checksum mismatch
If you see checksum errors, it means a previously applied migration was modified:
```bash
# Repair the history (use with caution)
./mvnw flyway:repair
```

### Schema validation failed
If Hibernate validation fails after migrations, ensure your entity classes match the database schema.

## Environment-Specific Migrations

For environment-specific migrations, you can use:

1. **Profile-specific locations**:
```properties
# application-dev.properties
spring.flyway.locations=classpath:db/migration,classpath:db/migration/dev

# application-prod.properties
spring.flyway.locations=classpath:db/migration,classpath:db/migration/prod
```

2. **Repeatable migrations** (prefix `R__`):
These run every time their checksum changes, useful for views or stored procedures.
