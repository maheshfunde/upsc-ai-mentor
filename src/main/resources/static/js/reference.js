// ============================================
// UPSC AI MENTOR - QUICK REFERENCE LOGIC
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    switchTab('articles');
});

function switchTab(tabName) {
    // Hide all content sections
    document.querySelectorAll('.reference-content').forEach(el => {
        el.style.display = 'none';
    });

    // Show selected tab
    const tabContent = document.getElementById('tab-' + tabName);
    if (tabContent) {
        tabContent.style.display = 'block';
    }

    // Update tab buttons
    document.querySelectorAll('.reference-tab').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.tab === tabName) {
            btn.classList.add('active');
        }
    });

    // Clear search
    const searchInput = document.getElementById('referenceSearch');
    if (searchInput) {
        searchInput.value = '';
        resetFilters();
    }
}

function filterReferences() {
    const query = document.getElementById('referenceSearch').value.toLowerCase().trim();

    if (!query) {
        resetFilters();
        return;
    }

    // Search across all visible cards
    const activeTab = document.querySelector('.reference-tab.active').dataset.tab;
    const tabContent = document.getElementById('tab-' + activeTab);
    if (!tabContent) return;

    const cards = tabContent.querySelectorAll('.reference-card');
    let visibleCount = 0;

    cards.forEach(card => {
        const searchData = card.dataset.search || '';
        const cardText = (card.textContent + ' ' + searchData).toLowerCase();

        if (cardText.includes(query)) {
            card.style.display = '';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    // Show/hide parent sections
    tabContent.querySelectorAll('.reference-section').forEach(section => {
        const visibleCards = section.querySelectorAll('.reference-card:not([style*="display: none"])');
        section.style.display = visibleCards.length > 0 ? '' : 'none';
    });
}

function resetFilters() {
    const activeTab = document.querySelector('.reference-tab.active');
    if (!activeTab) return;

    const tabContent = document.getElementById('tab-' + activeTab.dataset.tab);
    if (!tabContent) return;

    tabContent.querySelectorAll('.reference-card').forEach(card => {
        card.style.display = '';
    });
    tabContent.querySelectorAll('.reference-section').forEach(section => {
        section.style.display = '';
    });
}
