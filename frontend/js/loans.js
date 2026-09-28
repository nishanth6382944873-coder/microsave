/**
 * MicroSave – Loans Page JavaScript Logic
 */

const API_BASE = 'http://localhost:8080/api';

let currentAvailablePool = 0;
let memberSummaryCache = {};

document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today
    const dateInput = document.getElementById('loan-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    loadGroupsAndInit();
    loadLoans();
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
 * Load groups and initialize group pool
 */
async function loadGroupsAndInit() {
    try {
        const response = await fetch(`${API_BASE}/groups`);
        if (!response.ok) throw new Error('Failed to load groups');
        const groups = await response.json();
        const select = document.getElementById('loan-group');
        select.innerHTML = '<option value="">-- Select Group --</option>';

        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name;
            select.appendChild(opt);
        });

        if (groups.length > 0) {
            select.selectedIndex = 1;
            onGroupChange(select.value);
        }
    } catch (err) {
        console.error(err);
        showAlert('Could not load group list. Please ensure backend is running.', 'error');
    }
}

/**
 * Handle group change: refresh available pool and members for that group
 */
async function onGroupChange(groupId) {
    if (!groupId) return;
    await fetchAvailablePool(groupId);
    await loadMembersForGroup(groupId);
}

/**
 * Fetch available pool: RULE 1 (Total Contributions - Total Outstanding Loans)
 */
async function fetchAvailablePool(groupId) {
    try {
        const response = await fetch(`${API_BASE}/loans/pool/${groupId}`);
        if (!response.ok) throw new Error('Failed to fetch available pool');
        const data = await response.json();
        currentAvailablePool = data.availablePool !== undefined ? data.availablePool : 0;

        const poolEl = document.getElementById('display-available-pool');
        if (poolEl) {
            poolEl.textContent = formatCurrency(currentAvailablePool);
        }

        // Re-validate current amount input if any
        const amountInput = document.getElementById('loan-amount');
        if (amountInput && amountInput.value) {
            validateLoanAmountAgainstPool(amountInput.value);
        }
    } catch (err) {
        console.error(err);
    }
}

/**
 * Live validation as user types requested loan amount
 */
function validateLoanAmountAgainstPool(enteredAmount) {
    const warning = document.getElementById('pool-warning');
    const amount = parseFloat(enteredAmount);

    if (!isNaN(amount) && amount > currentAvailablePool) {
        warning.style.display = 'block';
        warning.textContent = `⚠️ Loan amount (${formatCurrency(amount)}) exceeds available group pool (${formatCurrency(currentAvailablePool)}).`;
    } else {
        warning.style.display = 'none';
    }
}

/**
 * Load members for selected group
 */
async function loadMembersForGroup(groupId) {
    const select = document.getElementById('loan-member');
    try {
        const response = await fetch(`${API_BASE}/members?groupId=${groupId}`);
        if (!response.ok) throw new Error('Failed to load members');
        const members = await response.json();

        select.innerHTML = '<option value="">-- Select Member --</option>';
        if (members.length === 0) {
            select.innerHTML = '<option value="">No members in this group</option>';
            return;
        }

        members.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = `${m.name} (#${m.id})`;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error(err);
        showAlert('Could not load members for the selected group.', 'error');
    }
}

/**
 * Check if selected member already has an active loan (RULE 2 preview)
 */
async function checkMemberLoanStatus(memberId) {
    const warningEl = document.getElementById('member-warning');
    if (!memberId) {
        warningEl.style.display = 'none';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/members/${memberId}/summary`);
        if (!response.ok) return;
        const summary = await response.json();

        if (summary.outstandingLoan > 0) {
            warningEl.style.display = 'block';
            warningEl.textContent = `⚠️ Warning: Member has an active unpaid loan with ₹${summary.outstandingLoan} outstanding.`;
        } else {
            warningEl.style.display = 'none';
        }
    } catch (err) {
        console.error(err);
    }
}

/**
 * Submit loan disbursement request to backend
 */
async function handleLoanSubmit(event) {
    event.preventDefault();
    clearAlert();

    const groupId = document.getElementById('loan-group').value;
    const memberId = document.getElementById('loan-member').value;
    const amount = parseFloat(document.getElementById('loan-amount').value);
    const date = document.getElementById('loan-date').value;
    const description = document.getElementById('loan-description').value.trim();

    if (!groupId || !memberId) {
        showAlert('Please select both a Group and a Member.', 'warning');
        return;
    }

    if (isNaN(amount) || amount <= 0) {
        showAlert('Loan amount must be greater than zero.', 'error');
        return;
    }

    const payload = {
        groupId: parseInt(groupId),
        memberId: parseInt(memberId),
        amount: amount,
        loanDate: date || null,
        description: description || 'Internal group credit'
    };

    try {
        const response = await fetch(`${API_BASE}/loans`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Disbursal rejected (HTTP ${response.status})`);
        }

        const result = await response.json();
        showAlert(`🎉 Loan of <strong>${formatCurrency(result.amount)}</strong> successfully disbursed to <strong>${result.memberName}</strong>!`, 'success');

        // Reset form
        document.getElementById('loan-amount').value = '';
        document.getElementById('loan-description').value = '';
        document.getElementById('member-warning').style.display = 'none';
        document.getElementById('pool-warning').style.display = 'none';

        // Refresh data
        fetchAvailablePool(groupId);
        loadLoans();
    } catch (err) {
        console.error(err);
        showAlert(`❌ ${err.message}`, 'error');
    }
}

let allLoans = [];

/**
 * Fetch and render all loans
 */
async function loadLoans() {
    const tableBody = document.getElementById('loans-table-body');
    try {
        const response = await fetch(`${API_BASE}/loans`);
        if (!response.ok) throw new Error('Failed to load loans');
        allLoans = await response.json();

        // Sort latest first
        allLoans.sort((a, b) => b.id - a.id);

        applyLoanFilters();
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td colspan="8" class="table-empty" style="color: var(--danger);">Failed to load loans: ${err.message}</td></tr>`;
    }
}

/**
 * Render loans list into HTML table
 */
function renderLoansTable(list) {
    const tableBody = document.getElementById('loans-table-body');
    const badge = document.getElementById('loans-count-badge');
    if (badge) {
        badge.textContent = `${list.length} Loan${list.length === 1 ? '' : 's'}`;
    }

    if (list.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="8" class="table-empty">No loans match current filter criteria.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    list.forEach(loan => {
        const tr = document.createElement('tr');
        const isActive = loan.status === 'ACTIVE';
        const badgeClass = isActive ? 'badge-active' : 'badge-closed';

        tr.innerHTML = `
            <td><strong>#${loan.id}</strong></td>
            <td><strong>${loan.memberName || 'Member #' + loan.memberId}</strong></td>
            <td>${formatCurrency(loan.amount)}</td>
            <td><strong style="color: ${loan.outstandingAmount > 0 ? 'var(--danger)' : 'var(--success)'};">${formatCurrency(loan.outstandingAmount)}</strong></td>
            <td>${loan.loanDate || '-'}</td>
            <td><span class="badge ${badgeClass}">${loan.status}</span></td>
            <td>${loan.description || '<span style="color:var(--text-light);">-</span>'}</td>
            <td style="text-align: right; white-space: nowrap;">
                ${isActive ? `<a href="repayments.html?loanId=${loan.id}" class="btn btn-primary btn-sm">🔄 Repay</a>` : '<span style="color:var(--text-light); font-size:0.8rem;">Fully Paid</span>'}
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

/**
 * Filter loans by status and search keyword
 */
function applyLoanFilters() {
    const statusFilter = document.getElementById('loan-status-filter')?.value || 'ALL';
    const query = (document.getElementById('loan-search')?.value || '').toLowerCase().trim();

    let filtered = allLoans;

    if (statusFilter !== 'ALL') {
        filtered = filtered.filter(l => l.status === statusFilter);
    }

    if (query) {
        filtered = filtered.filter(l => 
            (l.memberName && l.memberName.toLowerCase().includes(query)) ||
            (l.description && l.description.toLowerCase().includes(query)) ||
            String(l.id).includes(query)
        );
    }

    renderLoansTable(filtered);
}

function reloadLoansPage() {
    const groupSelect = document.getElementById('loan-group');
    if (groupSelect && groupSelect.value) {
        onGroupChange(groupSelect.value);
    }
    loadLoans();
}
