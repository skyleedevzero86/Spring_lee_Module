document.addEventListener('DOMContentLoaded', function() {
    initializeComparisonBadge();
    initializeCopyButtons();
});

function initializeComparisonBadge() {
    const comparisonBadge = document.querySelector('.comparison-badge');
    if (comparisonBadge) {
        const text = comparisonBadge.textContent.trim();
        const similarityScore = parseFloat(text.replace('%', ''));
        if (!isNaN(similarityScore)) {
        }
    }
}

function initializeCopyButtons() {
    const ocrTextElements = document.querySelectorAll('.ocr-text');
    ocrTextElements.forEach(function(element) {
        element.style.cursor = 'text';
    });
}