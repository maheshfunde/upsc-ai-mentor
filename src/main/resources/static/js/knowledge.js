const API_BASE = '/api';

function getUser() {
    const data = localStorage.getItem('upscMentorUser');
    return data ? JSON.parse(data) : null;
}

document.addEventListener('DOMContentLoaded', () => {
    const user = getUser();
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }
    loadDocuments();
    loadStats();
});

function handleDragOver(e) {
    e.preventDefault();
    e.currentTarget.classList.add('dragover');
}

function handleDragLeave(e) {
    e.currentTarget.classList.remove('dragover');
}

function handleDrop(e) {
    e.preventDefault();
    e.currentTarget.classList.remove('dragover');
    uploadFiles(e.dataTransfer.files);
}

function handleFileSelect(e) {
    uploadFiles(e.target.files);
}

async function uploadFiles(files) {
    const user = getUser();
    if (!user) return;

    document.getElementById('uploadProgress').style.display = 'block';

    for (const file of files) {
        try {
            const formData = new FormData();
            formData.append('userId', user.id);
            formData.append('file', file);

            const response = await fetch(`${API_BASE}/knowledge/upload`, { method: 'POST', body: formData });
            const data = await response.json();
            if (!data.success) alert('Upload failed: ' + data.message);
        } catch (error) {
            alert('Upload error: ' + error.message);
        }
    }

    document.getElementById('uploadProgress').style.display = 'none';
    loadDocuments();
    loadStats();
}

async function loadDocuments() {
    const user = getUser();
    if (!user) return;

    try {
        const response = await fetch(`${API_BASE}/knowledge/documents?userId=${user.id}`);
        const data = await response.json();
        const listEl = document.getElementById('documentList');

        if (!data.data || data.data.length === 0) {
            listEl.innerHTML = '<p class="analysis-placeholder">No documents uploaded yet. Upload PDFs, notes, or images to get started.</p>';
            return;
        }

        listEl.innerHTML = data.data.map(doc => {
            const icon = doc.fileType === 'PDF' ? '📕' : doc.fileType === 'IMAGE' ? '🖼️' : '📄';
            const size = doc.fileSizeKb ? (doc.fileSizeKb > 1024 ? (doc.fileSizeKb/1024).toFixed(1) + ' MB' : doc.fileSizeKb + ' KB') : '';
            return `
                <div class="doc-item" id="doc-${doc.id}">
                    <div class="doc-info">
                        <span class="doc-icon">${icon}</span>
                        <div>
                            <div class="doc-name">${escapeHtml(doc.filename)}</div>
                            <div class="doc-meta">${doc.chunkCount || 0} chunks · ${size}</div>
                        </div>
                    </div>
                    <span class="doc-status ${doc.status}">${doc.status}</span>
                    <div class="doc-actions">
                        ${doc.status === 'READY' ? `<button class="preview-btn" onclick="previewDoc(${doc.id})">Preview</button>` : ''}
                        <button onclick="deleteDoc(${doc.id})">Delete</button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Failed to load documents:', error);
    }
}

async function loadStats() {
    const user = getUser();
    if (!user) return;

    try {
        const response = await fetch(`${API_BASE}/knowledge/stats?userId=${user.id}`);
        const data = await response.json();
        if (data.data) {
            document.getElementById('docCount').textContent = data.data.documentCount || 0;
            document.getElementById('chunkCount').textContent = data.data.totalChunks || 0;
            const kb = data.data.storageUsedKb || 0;
            document.getElementById('storageUsed').textContent = kb > 1024 ? (kb / 1024).toFixed(1) + ' MB' : kb + ' KB';
        }
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}

async function previewDoc(id) {
    const user = getUser();
    if (!user) return;
    try {
        const response = await fetch(`${API_BASE}/knowledge/documents/${id}/preview?userId=${user.id}`);
        const data = await response.json();
        if (data.data && data.data.text) showModal(data.data.text);
    } catch (error) {
        alert('Preview failed: ' + error.message);
    }
}

function showModal(text) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
    overlay.innerHTML = `
        <div class="modal-content">
            <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">✕</button>
            <h3>Document Preview</h3>
            <pre>${escapeHtml(text)}</pre>
        </div>
    `;
    document.body.appendChild(overlay);
}

async function deleteDoc(id) {
    if (!confirm('Delete this document and remove it from the knowledge base?')) return;
    const user = getUser();
    if (!user) return;
    try {
        const response = await fetch(`${API_BASE}/knowledge/documents/${id}?userId=${user.id}`, { method: 'DELETE' });
        const data = await response.json();
        if (data.success) {
            document.getElementById('doc-' + id)?.remove();
            loadStats();
        } else {
            alert('Delete failed: ' + data.message);
        }
    } catch (error) {
        alert('Delete error: ' + error.message);
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

setInterval(() => {
    if (document.querySelectorAll('.doc-status.PROCESSING').length > 0) loadDocuments();
}, 5000);
