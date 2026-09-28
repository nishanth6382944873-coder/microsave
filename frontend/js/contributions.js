/**
 * MicroSave – Contributions Page JavaScript Logic
 */

const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    // Set default date to today
    const dateInput = document.getElementById('contrib-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    loadGroupsAndMembers();
    loadContributions();
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
 * Load groups and trigger member loading for the first group
 */
async function loadGroupsAndMembers() {
    try {
        const response = await fetch(`${API_BASE}/groups`);
        if (!response.ok) throw new Error('Failed to load groups');
        const groups = await response.json();
        const select = document.getElementById('contrib-group');
        select.innerHTML = '<option value="">-- Select Group --</option>';

        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name;
            select.appendChild(opt);
        });

        if (groups.length > 0) {
            select.selectedIndex = 1;
            filterMembersByGroup(select.value);
        }
    } catch (err) {
        console.error(err);
        showAlert('Could not load group list. Please ensure backend is running.', 'error');
    }
}

/**
 * Filter members dropdown according to selected group
 */
async function filterMembersByGroup(groupId) {
    const select = document.getElementById('contrib-member');
    if (!groupId) {
        select.innerHTML = '<option value="">-- Select Member --</option>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/members?groupId=${groupId}`);
        if (!response.ok) throw new Error('Failed to load members for group');
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
 * Submit savings contribution
 */
async function handleContributionSubmit(event) {
    event.preventDefault();
    clearAlert();

    const groupId = document.getElementById('contrib-group').value;
    const memberId = document.getElementById('contrib-member').value;
    const amount = parseFloat(document.getElementById('contrib-amount').value);
    const date = document.getElementById('contrib-date').value;
    const description = document.getElementById('contrib-description').value.trim();

    if (!groupId || !memberId) {
        showAlert('Please select both a Group and a Member.', 'warning');
        return;
    }

    if (isNaN(amount) || amount <= 0) {
        showAlert('Contribution amount must be greater than zero.', 'error');
        return;
    }

    const payload = {
        groupId: parseInt(groupId),
        memberId: parseInt(memberId),
        amount: amount,
        contributionDate: date || null,
        description: description || 'Regular savings deposit'
    };

    try {
        const response = await fetch(`${API_BASE}/contributions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Failed to record contribution (HTTP ${response.status})`);
        }

        const result = await response.json();
        showAlert(`✅ Successfully recorded contribution of <strong>${formatCurrency(result.amount)}</strong> for member <strong>${result.memberName}</strong>!`, 'success');

        // Reset form fields except group and date
        document.getElementById('contrib-amount').value = '';
        document.getElementById('contrib-description').value = '';

        loadContributions();
    } catch (err) {
        console.error(err);
        showAlert(`Error: ${err.message}`, 'error');
    }
}

let allContributions = [];

/**
 * Fetch and render all contributions
 */
async function loadContributions() {
    const tableBody = document.getElementById('contributions-table-body');
    try {
        const response = await fetch(`${API_BASE}/contributions`);
        if (!response.ok) throw new Error('Failed to load contributions');
        allContributions = await response.json();

        // Sort latest first
        allContributions.sort((a, b) => b.id - a.id);

        renderContributionsTable(allContributions);
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td colspan="6" class="table-empty" style="color: var(--danger);">Failed to load contributions: ${err.message}</td></tr>`;
    }
}

/**
 * Render contributions list into table
 */
function renderContributionsTable(list) {
    const tableBody = document.getElementById('contributions-table-body');
    const badge = document.getElementById('contrib-total-badge');

    // Calculate sum of displayed records
    const totalSum = list.reduce((sum, item) => sum + (item.amount || 0), 0);
    if (badge) {
        badge.textContent = `Total: ${formatCurrency(totalSum)}`;
    }

    if (list.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="table-empty">No savings contributions found.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    list.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${c.id}</strong></td>
            <td><strong>${c.memberName || 'Member #' + c.memberId}</strong></td>
            <td><span class="badge badge-group">${c.groupName || 'SHG'}</span></td>
            <td><strong style="color: var(--primary);">${formatCurrency(c.amount)}</strong></td>
            <td>${c.contributionDate || '-'}</td>
            <td>${c.description || '<span style="color:var(--text-light);">-</span>'}</td>
        `;
        tableBody.appendChild(tr);
    });
}

/**
 * Filter contributions by member name or description
 */
function filterContributionsTable(query) {
    if (!query) {
        renderContributionsTable(allContributions);
        return;
    }
    const q = query.toLowerCase();
    const filtered = allContributions.filter(c => 
        (c.memberName && c.memberName.toLowerCase().includes(q)) ||
        (c.description && c.description.toLowerCase().includes(q)) ||
        (c.groupName && c.groupName.toLowerCase().includes(q))
    );
    renderContributionsTable(filtered);
}
