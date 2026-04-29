-- =====================================================================
-- Seed: populate career_nodes descriptions + estimated_hours
-- Run once with:
--   mysql -u root -p career_db < backend/scripts/seed_career_nodes.sql
-- Idempotent: re-running just overwrites the same rows.
-- =====================================================================

UPDATE career_nodes
SET description='Master variables, control flow, OOP, collections, generics, and exception handling. The bedrock for everything that follows.',
    estimated_hours=80
WHERE node_id=1;

UPDATE career_nodes
SET description='Build REST APIs with Spring Boot. Cover dependency injection, MVC, JPA, transactions, and basic error handling.',
    estimated_hours=60
WHERE node_id=2;

UPDATE career_nodes
SET description='Design normalized schemas, write efficient SQL, understand indexes, transactions, and isolation levels.',
    estimated_hours=40
WHERE node_id=3;

UPDATE career_nodes
SET description='Service discovery, gateway, circuit breakers, distributed config, and how microservices communicate at scale.',
    estimated_hours=70
WHERE node_id=4;

UPDATE career_nodes
SET description='Semantic markup, modern CSS layouts (flexbox + grid), responsive design, and accessibility fundamentals.',
    estimated_hours=40
WHERE node_id=5;

UPDATE career_nodes
SET description='ES2015+, async/await, the event loop, modules, and the DOM API. Skip nothing here.',
    estimated_hours=60
WHERE node_id=6;

UPDATE career_nodes
SET description='Composition API, reactivity, single-file components, Pinia state, Vue Router, and build tooling with Vite.',
    estimated_hours=70
WHERE node_id=7;

-- Verify
SELECT node_id, name, level, parent_id, LEFT(description, 60) AS preview, estimated_hours
FROM career_nodes ORDER BY path_id, sort_order, node_id;
