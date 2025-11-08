// Enhanced main.js with form validation fixes
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    console.log('SmartQuizApp initialized');
    autoDismissAlerts();
    enhanceForms();
}

function autoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        if (!alert.classList.contains('alert-permanent')) {
            setTimeout(() => {
                if (alert.parentNode) {
                    const bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                }
            }, 5000);
        }
    });
}

function enhanceForms() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        // Remove any existing listeners to prevent duplicates
        form.removeEventListener('submit', handleFormSubmit);
        form.addEventListener('submit', handleFormSubmit);
        
        // Enable/disable submit button based on form validity
        const submitBtn = form.querySelector('button[type="submit"]');
        if (submitBtn) {
            form.addEventListener('input', function() {
                submitBtn.disabled = !form.checkValidity();
            });
            // Initial state
            submitBtn.disabled = !form.checkValidity();
        }
    });
}

function handleFormSubmit(e) {
    const form = e.target;
    const submitBtn = form.querySelector('button[type="submit"]');
    
    if (submitBtn && form.checkValidity()) {
        // Only show processing if form is valid
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
        
        // Form will submit normally - don't prevent default
        console.log('Form submitting...');
    } else {
        // If form is invalid, let browser show validation messages
        console.log('Form invalid, showing validation messages');
    }
}

// Utility functions
window.SmartQuizApp = {
    formatTime: function(seconds) {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },
    initializeApp: initializeApp
};