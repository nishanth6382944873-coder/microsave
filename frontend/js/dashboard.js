/**
 * MicroSave – Self-Help Group Savings Tracker
 * Dashboard JavaScript Logic
 */

const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    loadGroups();
});

/**
 * Format numeric value as Indian Rupee (INR) currency string
 */
function formatCurrency(amount) {
    if (amount === undefined || amount === null || isNaN(amount)) {
        return '₹0.00';
    }
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2
    }).format(amount);
}

/**
 * Show a dismissible alert notification banner
 */
function showAlert(message, type = 'error') {
    const alertBox = document.getElementById('alert-box');
    if (!alertBox) return;

    alertBox.innerHTML = `
        <div class="alert alert-${type}">
            <span>${message}</span>
            <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
        </div>
    `;
}

/**
 * Clear alert notification banner
 */
function clearAlert() {
    const alertBox = document.getElementById('alert-box');
    if (alertBox) alertBox.innerHTML = '';
}

/**
 * Fetch all registered SHG groups to populate dropdown
 */
async function loadGroups() {
    try {
        const response = await fetch(`${API_BASE}/groups`);
        if (!response.ok) {
            throw new Error(`Failed to load groups (Status: ${response.status})`);
        }
        const groups = await response.json();
        const selector = document.getElementById('group-selector');
        selector.innerHTML = '';

        if (groups.length === 0) {
            selector.innerHTML = '<option value="">No Groups Found</option>';
            return;
        }

        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name;
            selector.appendChild(opt);
        });

        // Load dashboard for first group
        const selectedId = selector.value;
        if (selectedId) {
            loadDashboardData(selectedId);
        }
    } catch (err) {
        console.error('Error fetching groups:', err);
        showAlert(`⚠️ Cannot connect to backend at <strong>${API_BASE}</strong>. Ensure your Spring Boot backend is running on port 8080.`, 'error');
    }
}

/**
 * Load dashboard metrics for specified groupId
 */
async function loadDashboardData(groupId) {
    if (!groupId) return;
    clearAlert();

    try {
        const response = await fetch(`${API_BASE}/dashboard/${groupId}`);
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }

        const data = await response.json();

        // Update Group Title
        const titleEl = document.getElementById('display-group-title');
        if (titleEl) {
            titleEl.textContent = `${data.groupName || 'SHG'} – Financial Overview`;
        }

        // Update Metric Cards
        document.getElementById('val-total-members').textContent = data.totalMembers ?? 0;
        document.getElementById('val-total-savings').textContent = formatCurrency(data.totalContributions);
        document.getElementById('val-total-loans').textContent = formatCurrency(data.totalLoans);
        document.getElementById('val-outstanding-loans').textContent = formatCurrency(data.totalOutstandingLoans);
        document.getElementById('val-available-pool').textContent = formatCurrency(data.availablePool);

        // Load recent deposits and loans
        loadRecentActivity(groupId);

    } catch (err) {
        console.error('Error fetching dashboard summary:', err);
        showAlert(`Failed to fetch dashboard data: ${err.message}`, 'error');
    }
}

/**
 * Fetch and render recent contributions and loans on the dashboard
 */
async function loadRecentActivity(groupId) {
    const contribBody = document.getElementById('recent-contrib-body');
    const loansBody = document.getElementById('recent-loans-body');

    // 1. Fetch recent contributions
    try {
        const cRes = await fetch(`${API_BASE}/contributions?groupId=${groupId}`);
        if (cRes.ok) {
            const list = await cRes.json();
            list.sort((a, b) => b.id - a.id);
            const top5 = list.slice(0, 5);

            if (top5.length === 0) {
                contribBody.innerHTML = '<tr><td colspan="3" class="table-empty">No savings deposits recorded yet.</td></tr>';
            } else {
                contribBody.innerHTML = '';
                top5.forEach(c => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><strong>${c.memberName || 'Member #' + c.memberId}</strong></td>
                        <td><strong style="color: var(--primary);">${formatCurrency(c.amount)}</strong></td>
                        <td>${c.contributionDate || '-'}</td>
                    `;
                    contribBody.appendChild(tr);
                });
            }
        }
    } catch (e) {
        console.error('Error loading recent contributions:', e);
    }

    // 2. Fetch recent loans
    try {
        const lRes = await fetch(`${API_BASE}/loans?groupId=${groupId}`);
        if (lRes.ok) {
            const list = await lRes.json();
            list.sort((a, b) => b.id - a.id);
            const top5 = list.slice(0, 5);

            if (top5.length === 0) {
                loansBody.innerHTML = '<tr><td colspan="3" class="table-empty">No loans disbursed yet.</td></tr>';
            } else {
                loansBody.innerHTML = '';
                top5.forEach(l => {
                    const tr = document.createElement('tr');
                    const isActive = l.status === 'ACTIVE';
                    const badgeClass = isActive ? 'badge-active' : 'badge-closed';
                    tr.innerHTML = `
                        <td><strong>${l.memberName || 'Member #' + l.memberId}</strong></td>
                        <td><strong style="color: ${l.outstandingAmount > 0 ? 'var(--danger)' : 'var(--success)'};">${formatCurrency(l.outstandingAmount)}</strong></td>
                        <td><span class="badge ${badgeClass}">${l.status}</span></td>
                    `;
                    loansBody.appendChild(tr);
                });
            }
        }
    } catch (e) {
        console.error('Error loading recent loans:', e);
    }
}

/**
 * Reload dashboard on button click
 */
function reloadDashboard() {
    const selector = document.getElementById('group-selector');
    if (selector && selector.value) {
        loadDashboardData(selector.value);
    } else {
        loadGroups();
    }
}
