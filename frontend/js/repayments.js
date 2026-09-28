/**
 * MicroSave – Repayments Page JavaScript Logic
 */

const API_BASE = 'http://localhost:8080/api';

let activeLoansMap = {};

document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today
    const dateInput = document.getElementById('rep-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    loadActiveLoansDropdown().then(() => {
        // If loanId is in URL query (e.g. repayments.html?loanId=1), select it automatically
        const urlParams = new URLSearchParams(window.location.search);
        const queryLoanId = urlParams.get('loanId');
        if (queryLoanId) {
            const select = document.getElementById('rep-loan');
            select.value = queryLoanId;
            onLoanSelectChange(queryLoanId);
        }
    });

    loadRepaymentsHistory();
});

function showAlert(message, type = 'error') {
    const alertBox = document.getElementById('alert-box');
    if (!alertBox) return;
    alertBox.innerHTML = `
        <div class="alert alert-${type}">
            <span>${message}</span>
            <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
        </div>
    `;
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function clearAlert() {
    const alertBox = document.getElementById('alert-box');
    if (alertBox) alertBox.innerHTML = '';
}

function formatCurrency(amount) {
    if (amount === undefined || amount === null || isNaN(amount)) return '₹0.00';
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2
    }).format(amount);
}

/**
 * Load all active loans into the dropdown
 */
async function loadActiveLoansDropdown() {
    try {
        const response = await fetch(`${API_BASE}/loans`);
        if (!response.ok) throw new Error('Failed to load loans');
        const loans = await response.json();

        activeLoansMap = {};
        const select = document.getElementById('rep-loan');
        select.innerHTML = '<option value="">-- Select an Active Loan --</option>';

        // Filter only ACTIVE loans with outstanding > 0
        const activeLoans = loans.filter(l => l.status === 'ACTIVE' && l.outstandingAmount > 0);

        if (activeLoans.length === 0) {
            select.innerHTML = '<option value="">No active loans pending repayment</option>';
            return;
        }

        activeLoans.forEach(l => {
            activeLoansMap[l.id] = l;
            const opt = document.createElement('option');
            opt.value = l.id;
            opt.textContent = `Loan #${l.id} - ${l.memberName} (Disbursed: ${formatCurrency(l.amount)} | Outstanding: ${formatCurrency(l.outstandingAmount)})`;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error(err);
        showAlert('Could not load loans. Please ensure backend is running.', 'error');
    }
}

/**
 * Handle loan dropdown change
 */
function onLoanSelectChange(loanId) {
    const badgeEl = document.getElementById('loan-details-badge');
    const warningEl = document.getElementById('repay-warning');
    const previewEl = document.getElementById('repay-preview');
    const amountInput = document.getElementById('rep-amount');
    const payFullBtn = document.getElementById('btn-pay-full');

    warningEl.style.display = 'none';
    if (previewEl) previewEl.style.display = 'none';

    if (!loanId || !activeLoansMap[loanId]) {
        badgeEl.style.display = 'none';
        if (payFullBtn) payFullBtn.style.display = 'none';
        return;
    }

    const loan = activeLoansMap[loanId];
    badgeEl.style.display = 'block';
    if (payFullBtn) payFullBtn.style.display = 'inline-flex';

    badgeEl.innerHTML = `
        <strong>Borrower:</strong> ${loan.memberName} | 
        <strong>Original Loan:</strong> ${formatCurrency(loan.amount)} | 
        <strong>Current Outstanding:</strong> <span style="color:var(--danger); font-weight:700;">${formatCurrency(loan.outstandingAmount)}</span>
    `;

    // Max amount hint
    amountInput.max = loan.outstandingAmount;
    if (amountInput.value) {
        validateRepaymentAmount(amountInput.value);
    }
}

/**
 * Quick fill full outstanding balance
 */
function payFullOutstanding() {
    const loanId = document.getElementById('rep-loan').value;
    if (!loanId || !activeLoansMap[loanId]) return;
    const loan = activeLoansMap[loanId];
    const amountInput = document.getElementById('rep-amount');
    amountInput.value = loan.outstandingAmount;
    validateRepaymentAmount(loan.outstandingAmount);
}

/**
 * Live validation for repayment amount
 * RULE 6: Repayment cannot exceed outstanding loan
 */
function validateRepaymentAmount(enteredAmount) {
    const loanId = document.getElementById('rep-loan').value;
    const warningEl = document.getElementById('repay-warning');
    const previewEl = document.getElementById('repay-preview');
    const amount = parseFloat(enteredAmount);

    if (!loanId || !activeLoansMap[loanId]) {
        warningEl.style.display = 'none';
        if (previewEl) previewEl.style.display = 'none';
        return;
    }

    const loan = activeLoansMap[loanId];

    if (isNaN(amount) || amount <= 0) {
        warningEl.style.display = 'block';
        warningEl.textContent = '⚠️ Repayment amount must be greater than zero.';
        if (previewEl) previewEl.style.display = 'none';
    } else if (amount > loan.outstandingAmount) {
        warningEl.style.display = 'block';
        warningEl.textContent = `⚠️ Repayment amount (${formatCurrency(amount)}) cannot exceed outstanding loan (${formatCurrency(loan.outstandingAmount)}).`;
        if (previewEl) previewEl.style.display = 'none';
    } else {
        warningEl.style.display = 'none';
        if (previewEl) {
            previewEl.style.display = 'block';
            const remaining = Math.max(0, loan.outstandingAmount - amount);
            if (remaining === 0) {
                previewEl.innerHTML = `🎉 <strong>Full Repayment:</strong> This installment will fully clear the debt. Loan status will automatically become <span class="badge badge-closed">CLOSED</span> (RULE 7).`;
            } else {
                previewEl.innerHTML = `ℹ️ Remaining outstanding after this payment: <strong>${formatCurrency(remaining)}</strong>.`;
            }
        }
    }
}

/**
 * Submit repayment
 */
async function handleRepaymentSubmit(event) {
    event.preventDefault();
    clearAlert();

    const loanId = document.getElementById('rep-loan').value;
    const amount = parseFloat(document.getElementById('rep-amount').value);
    const date = document.getElementById('rep-date').value;
    const description = document.getElementById('rep-description').value.trim();

    if (!loanId) {
        showAlert('Please select an active loan.', 'warning');
        return;
    }

    if (isNaN(amount) || amount <= 0) {
        showAlert('Repayment amount must be greater than zero.', 'error');
        return;
    }

    const payload = {
        loanId: parseInt(loanId),
        amount: amount,
        repaymentDate: date || null,
        description: description || 'Installment repayment'
    };

    try {
        const response = await fetch(`${API_BASE}/repayments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Repayment rejected (HTTP ${response.status})`);
        }

        const result = await response.json();

        if (result.loanStatus === 'CLOSED') {
            showAlert(`🎉 Payment of <strong>${formatCurrency(result.amount)}</strong> recorded! The loan is now <strong>FULLY REPAID</strong> and status has been updated to <strong>CLOSED</strong>!`, 'success');
        } else {
            showAlert(`✅ Payment of <strong>${formatCurrency(result.amount)}</strong> recorded successfully for <strong>${result.memberName}</strong>! Remaining outstanding: <strong>${formatCurrency(result.outstandingAmount)}</strong>.`, 'success');
        }

        // Reset form
        document.getElementById('rep-amount').value = '';
        document.getElementById('rep-description').value = '';
        document.getElementById('loan-details-badge').style.display = 'none';
        document.getElementById('repay-warning').style.display = 'none';

        // Refresh dropdown and history
        await loadActiveLoansDropdown();
        loadRepaymentsHistory();
    } catch (err) {
        console.error(err);
        showAlert(`❌ ${err.message}`, 'error');
    }
}

let allRepayments = [];

/**
 * Fetch and display all repayment transactions
 */
async function loadRepaymentsHistory() {
    const tableBody = document.getElementById('repayments-table-body');
    try {
        const response = await fetch(`${API_BASE}/repayments`);
        if (!response.ok) throw new Error('Failed to load repayments');
        allRepayments = await response.json();
        renderRepaymentsTable(allRepayments);
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td colspan="8" class="table-empty" style="color: var(--danger);">Failed to load repayments: ${err.message}</td></tr>`;
    }
}

/**
 * Render repayments list into HTML table
 */
function renderRepaymentsTable(list) {
    const tableBody = document.getElementById('repayments-table-body');
    const badge = document.getElementById('repay-total-badge');

    const totalRepaid = list.reduce((sum, item) => sum + (item.amount || 0), 0);
    if (badge) {
        badge.textContent = `Total Repaid: ${formatCurrency(totalRepaid)}`;
    }

    if (list.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="8" class="table-empty">No repayments found.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    list.forEach(r => {
        const tr = document.createElement('tr');
        const isClosed = r.loanStatus === 'CLOSED';
        const badgeClass = isClosed ? 'badge-closed' : 'badge-active';

        tr.innerHTML = `
            <td><strong>#${r.id}</strong></td>
            <td><strong>Loan #${r.loanId}</strong></td>
            <td><strong>${r.memberName || 'Member #' + r.memberId}</strong></td>
            <td>${formatCurrency(r.originalLoanAmount)}</td>
            <td><strong style="color: ${r.outstandingAmount > 0 ? 'var(--danger)' : 'var(--success)'};">${formatCurrency(r.outstandingAmount)}</strong></td>
            <td><strong style="color: var(--primary);">${formatCurrency(r.amount)}</strong></td>
            <td>${r.repaymentDate || '-'}</td>
            <td><span class="badge ${badgeClass}">${r.loanStatus || 'ACTIVE'}</span></td>
        `;
        tableBody.appendChild(tr);
    });
}

/**
 * Search filter for repayments
 */
function filterRepaymentsTable(query) {
    if (!query) {
        renderRepaymentsTable(allRepayments);
        return;
    }
    const q = query.toLowerCase();
    const filtered = allRepayments.filter(r => 
        (r.memberName && r.memberName.toLowerCase().includes(q)) ||
        String(r.loanId).includes(q) ||
        String(r.id).includes(q) ||
        (r.loanStatus && r.loanStatus.toLowerCase().includes(q))
    );
    renderRepaymentsTable(filtered);
}

function reloadRepaymentsPage() {
    loadActiveLoansDropdown();
    loadRepaymentsHistory();
}
