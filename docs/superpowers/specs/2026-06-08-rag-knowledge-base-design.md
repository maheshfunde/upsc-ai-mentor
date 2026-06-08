# RAG Knowledge Base System — Design Spec

**Date:** 2026-06-08
**Scope:** User-uploaded PDFs, text files, and images with OCR, stored in ChromaDB, retrieved via hybrid vector+BM25 search, integrated into chat responses with source citation badges.
**Status:** Pending review

---

## 1. Architecture Overview

```
User uploads PDF/Text/Image → Validate → Store file on disk
→ Extract text (PDFBox / FileReader / Tess4J OCR)
→ Split into 1000-token chunks with 200-token overlap
→ Generate embeddings (Ollama nomic-embed-text or OpenAI text-embedding-3-small)
→ Store in ChromaDB collection with metadata (user_id, document_id, chunk_index, source, page)

User asks question → Embed query → ChromaDB top-15 cosine similarity
→ BM25 keyword scoring on same results → Reciprocal rank fusion (60:40)
→ Top-5 chunks → Inject into AI prompt as <context_documents>
→ AI responds → Show 📚 Sources badge with document names and page numbers
```

**ChromaDB Strategy:** Single collection (`upsc_knowledge`) with `user_id` metadata filter. Not separate collections per user — simpler management, same query performance via ChromaDB's `where` filtering.

**Embedding Models:**
- Local (Ollama): `nomic-embed-text` (768-dim, fast, good quality)
- Online (OpenAI): `text-embedding-3-small` (1536-dim, best quality)

**Vector Store:** ChromaDB running locally via Docker. Default port 8000. Configurable via `application.yaml`.

---

## 2. Data Model

### New JPA Entity: `KnowledgeBaseDocument`

```
Table: knowledge_base_documents
- id (PK, auto-generated, Long)
- user_id (FK → users, NOT NULL)
- filename (VARCHAR 500, NOT NULL)
- file_type (VARCHAR 20, NOT NULL) — "PDF", "TEXT", "IMAGE"
- file_path (VARCHAR 1000, NOT NULL) — relative path under uploads/
- file_size_kb (Long)
- status (VARCHAR 20) — "UPLOADING", "PROCESSING", "READY", "FAILED", "PARTIAL"
- chunk_count (Integer, nullable)
- error_message (TEXT, nullable)
- created_at (LocalDateTime)
- updated_at (LocalDateTime)
```

### ChromaDB Collection: `upsc_knowledge`

Each stored embedding includes metadata:
```json
{
  "user_id": 1,
  "document_id": 5,
  "chunk_index": 12,
  "source": "Laxmikanth_Ch5.pdf",
  "page": 45,
  "file_type": "PDF"
}
```

---

## 3. Document Processing Pipeline

**Step 1: Upload Validation**
- Allowed types: `.pdf`, `.txt`, `.md`, `.png`, `.jpg`, `.jpeg`
- Max file size: 50MB
- Check for duplicate: same user + same filename → warn "Already uploaded"

**Step 2: File Storage**
- Store under `./data/uploads/{user_id}/{document_id}/{filename}`
- Generate document_id before saving

**Step 3: Text Extraction**
- PDF: Apache PDFBox (`PDFTextStripper`), extract page-by-page text
- Text/Markdown: Plain text read
- Image: Tess4J (Tesseract OCR), `doOCR()` on BufferedImage

**Step 4: Chunking**
- Split at ~1000 tokens (roughly 750 words) with 200-token overlap
- Preserve paragraph boundaries where possible (split on `\n\n` first, then by words)
- Each chunk gets metadata: `source`, `page` (PDF), `chunk_index`

**Step 5: Embedding**
- Call embedding model (local or online based on user's AI config)
- Batch embed chunks (10 per API call for OpenAI)

**Step 6: ChromaDB Storage**
- Insert embeddings with metadata into ChromaDB
- Update document status to `READY` (or `PARTIAL` if OCR was incomplete)

**Error Handling:**
- If any step fails → status `FAILED`, error_message set, user notified
- If OCR produces < 50 characters → status `PARTIAL`

---

## 4. Hybrid Retrieval Engine

**Query Flow:**
1. User sends question → generate query embedding
2. ChromaDB `query()`: top-15 results by cosine similarity, filtered by `user_id`
3. BM25: score the same 15 results against query text (keyword match)
4. Reciprocal Rank Fusion: `combined_score = 0.6 * vector_rank + 0.4 * bm25_rank`
5. Return top-5 chunks

**Why 60:40 split:** UPSC questions often contain specific terms ("Article 356", "73rd Amendment") that need exact matching (BM25), but also conceptual questions ("explain federalism") that need semantic matching (vector).

**BM25 Implementation:** Lightweight Java BM25 using Lucene's `BM25Similarity` or a simple implementation (no full Elasticsearch needed — only scoring pre-retrieved chunks).

---

## 5. Chat Integration

**AiModelRouterService Enhancement:**
- New method: `generateWithRag(User user, String prompt, String query, String documentFilter)`
- This method:
  1. Checks if user has any `READY` documents
  2. If yes → calls HybridRetrievalEngine.retrieve(query, user.getId())
  3. If chunks found → augments prompt with `<context_documents>` block
  4. Calls LLM with augmented prompt
  5. Returns both response and source list

**Prompt Augmentation Format:**
```xml
<context_documents>
  <document source="Laxmikanth_Ch5.pdf" page="45">
    Content of the relevant chunk...
  </document>
  <document source="Economy_Notes.txt">
    Another relevant chunk...
  </document>
</context_documents>

Instructions: Use the context documents above to provide accurate, specific answers. 
If the context contains relevant information, cite the source and page number.
If the context does not contain relevant information, answer from your general knowledge.
```

**Frontend:**
- Chat messages show source badge: `📚 Sources: Laxmikanth_Ch5.pdf (p.45), Economy_Notes.txt`
- Badge is clickable → opens modal showing exact excerpt used from each document
- If embedding model unavailable → badge shows `⚠️ Semantic search unavailable, using keyword search only`
- Chat input area shows indicator: `📚 3 documents indexed` when user has knowledge base content

---

## 6. API Endpoints

```
POST /api/knowledge/upload          → Upload file (multipart/form-data)
GET  /api/knowledge/documents       → List user's documents with status
DELETE /api/knowledge/documents/{id} → Delete document + remove from ChromaDB
GET  /api/knowledge/documents/{id}/preview → Preview extracted text (first 500 chars)
GET  /api/knowledge/stats           → User stats: total documents, chunks, file types
```

---

## 7. Configuration (application.yaml)

```yaml
rag:
  enabled: true
  chromadb:
    url: http://localhost:8000
    collection: upsc_knowledge
  embedding:
    model: nomic-embed-text  # or text-embedding-3-small for online
    provider: ollama          # or openai
  chunking:
    size: 1000
    overlap: 200
  retrieval:
    top-k: 15          # initial retrieval from ChromaDB
    final-k: 5         # after reciprocal rank fusion
    vector-weight: 0.6
    keyword-weight: 0.4
  upload:
    max-size-mb: 50
    allowed-types: pdf,txt,md,png,jpg,jpeg
  ocr:
    enabled: true
    language: eng+hin   # English + Hindi for UPSC context
```

---

## 8. New Page: Knowledge Base (`/knowledge`)

A dedicated page where users can:
- Upload files (drag-and-drop zone)
- See list of uploaded documents with status badges (Processing, Ready, Failed)
- Preview extracted text
- Delete documents
- View stats (total documents, total chunks, storage used)
- Quick actions: "Upload NCERT", "Upload PYQ Notes"

---

## 9. What This Does NOT Include

- Multi-user shared knowledge base (each user is isolated)
- Real-time collaborative editing
- Image generation or analysis beyond OCR
- Audio/video file support
- Automatic book download from internet (copyright concerns)
- Fine-tuning of embedding models
