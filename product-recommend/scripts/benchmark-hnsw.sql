
DROP INDEX IF EXISTS products_embedding_hnsw_cosine_idx;

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name
FROM products
WHERE embedding IS NOT NULL
ORDER BY embedding <=> '[0.01,0.02,0.03]'::vector
LIMIT 10;

CREATE INDEX IF NOT EXISTS products_embedding_hnsw_cosine_idx
	ON products
	USING hnsw (embedding vector_cosine_ops);

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name
FROM products
WHERE embedding IS NOT NULL
ORDER BY embedding <=> '[0.01,0.02,0.03]'::vector
LIMIT 10;
