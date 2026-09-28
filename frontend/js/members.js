/**
 * MicroSave – Members Page JavaScript Logic
 */

const API_BASE = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    loadGroupsDropdown();
    loadMembers();
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
 * Fetch groups to populate group selection dropdown
 */
async function loadGroupsDropdown() {
    try {
        const response = await fetch(`${API_BASE}/groups`);
        if (!response.ok) throw new Error('Failed to load groups');
        const groups = await response.json();
        const select = document.getElementById('member-group');
        select.innerHTML = '<option value="">-- Select Self-Help Group --</option>';

        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name;
            select.appendChild(opt);
        });
        if (groups.length > 0) {
            select.selectedIndex = 1; // Default to first available group
        }
    } catch (err) {
        console.error(err);
        showAlert('Could not load group list. Please ensure backend is running.', 'error');
    }
}

let allMembers = [];

/**
 * Fetch and display all members
 */
async function loadMembers() {
    const tableBody = document.getElementById('members-table-body');
    try {
        const response = await fetch(`${API_BASE}/members`);
        if (!response.ok) throw new Error('Failed to load members');
        allMembers = await response.json();
        renderMembersTable(allMembers);
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td colspan="7" class="table-empty" style="color: var(--danger);">Failed to load members: ${err.message}</td></tr>`;
    }
}

/**
 * Render members list into HTML table
 */
function renderMembersTable(members) {
    const tableBody = document.getElementById('members-table-body');
    const badge = document.getElementById('members-count-badge');
    if (badge) badge.textContent = `${members.length} Member${members.length === 1 ? '' : 's'}`;

    if (members.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="table-empty">No members found.</td></tr>';
        return;
    }

    tableBody.innerHTML = '';
    members.forEach(m => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${m.id}</strong></td>
            <td><strong>${m.name}</strong></td>
            <td>${m.phone}</td>
            <td>${m.email || '<span style="color:var(--text-light);">-</span>'}</td>
            <td>${m.address || '<span style="color:var(--text-light);">-</span>'}</td>
            <td><span class="badge badge-group">${m.groupName || 'SHG'}</span></td>
            <td style="text-align: right; white-space: nowrap;">
                <button class="btn btn-secondary btn-sm" onclick="showMemberSummary(${m.id})" title="View personal savings & loan balance">📊 Summary</button>
                <button class="btn btn-secondary btn-sm" onclick="editMember(${m.id}, '${escapeQuotes(m.name)}', '${escapeQuotes(m.phone)}', '${escapeQuotes(m.email || '')}', '${escapeQuotes(m.address || '')}', ${m.groupId})">✏️ Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.id}, '${escapeQuotes(m.name)}')">🗑️ Delete</button>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

/**
 * Live client-side search filter
 */
function filterMembersTable(query) {
    if (!query) {
        renderMembersTable(allMembers);
        return;
    }
    const q = query.toLowerCase();
    const filtered = allMembers.filter(m => 
        (m.name && m.name.toLowerCase().includes(q)) ||
        (m.phone && m.phone.toLowerCase().includes(q)) ||
        (m.email && m.email.toLowerCase().includes(q)) ||
        (m.address && m.address.toLowerCase().includes(q))
    );
    renderMembersTable(filtered);
}

function escapeQuotes(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

/**
 * Handle form submission for Add or Update
 */
async function handleMemberSubmit(event) {
    event.preventDefault();
    clearAlert();

    const id = document.getElementById('member-id').value;
    const name = document.getElementById('member-name').value.trim();
    const phone = document.getElementById('member-phone').value.trim();
    const email = document.getElementById('member-email').value.trim();
    const address = document.getElementById('member-address').value.trim();
    const groupId = document.getElementById('member-group').value;

    if (!name || !phone || !groupId) {
        showAlert('Please fill in all required fields (Name, Phone, and Group).', 'warning');
        return;
    }

    const payload = {
        name: name,
        phone: phone,
        email: email || null,
        address: address || null,
        groupId: parseInt(groupId)
    };

    try {
        let url = `${API_BASE}/members`;
        let method = 'POST';

        if (id) {
            url = `${API_BASE}/members/${id}`;
            method = 'PUT';
        }

        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Server error (${response.status})`);
        }

        const result = await response.json();
        showAlert(`✅ Member <strong>${result.name}</strong> ${id ? 'updated' : 'registered'} successfully!`, 'success');
        resetMemberForm();
        loadMembers();
    } catch (err) {
        console.error(err);
        showAlert(`Error: ${err.message}`, 'error');
    }
}

/**
 * Edit member: populate form
 */
function editMember(id, name, phone, email, address, groupId) {
    document.getElementById('member-id').value = id;
    document.getElementById('member-name').value = name;
    document.getElementById('member-phone').value = phone;
    document.getElementById('member-email').value = email || '';
    document.getElementById('member-address').value = address || '';
    document.getElementById('member-group').value = groupId;

    document.getElementById('form-heading').textContent = `✏️ Edit Member (#${id})`;
    document.getElementById('btn-submit').textContent = '💾 Update Member';
    document.getElementById('btn-cancel').style.display = 'inline-flex';

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Reset form back to Add Member state
 */
function resetMemberForm() {
    document.getElementById('member-form').reset();
    document.getElementById('member-id').value = '';
    document.getElementById('form-heading').textContent = '➕ Register New Member';
    document.getElementById('btn-submit').textContent = '➕ Add Member';
    document.getElementById('btn-cancel').style.display = 'none';
}

/**
 * Delete a member
 */
async function deleteMember(id, name) {
    if (!confirm(`Are you sure you want to delete member "${name}" (ID: ${id})?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/members/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `Failed to delete member (HTTP ${response.status})`);
        }

        showAlert(`Member "${name}" deleted successfully.`, 'success');
        loadMembers();
    } catch (err) {
        console.error(err);
        showAlert(`Delete failed: ${err.message}`, 'error');
    }
}

/**
 * Fetch and show Member Financial Summary in modal
 * Calls GET /api/members/{memberId}/summary
 */
async function showMemberSummary(memberId) {
    const modal = document.getElementById('summary-modal');
    const modalBody = document.getElementById('summary-modal-body');
    modal.classList.add('show');
    modalBody.innerHTML = '<div style="text-align: center; padding: 1.5rem;">Loading financial summary...</div>';

    try {
        const response = await fetch(`${API_BASE}/members/${memberId}/summary`);
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || `HTTP ${response.status}`);
        const data = await response.json();
        if (data.message && !data.memberName) {
            modalBody.innerHTML = `<div style="text-align: center; padding: 1.5rem; color: var(--text-secondary); font-size: 1.05rem;">ℹ️ ${data.message}</div>`;
            return;
        }

        modalBody.innerHTML = `
            <div style="text-align: center; margin-bottom: 1.25rem;">
                <h4 style="font-size: 1.3rem; color: var(--text-primary); margin-bottom: 0.25rem;">${data.memberName}</h4>
                <span class="badge badge-group">Member ID: #${data.memberId || memberId}</span>
            </div>
            <div>
                <div class="summary-item">
                    <span class="summary-label">Total Savings Deposited:</span>
                    <span class="summary-val" style="color: var(--primary);">${formatCurrency(data.totalSavings)}</span>
                </div>
                <div class="summary-item">
                    <span class="summary-label">Active Loan Disbursed:</span>
                    <span class="summary-val">${formatCurrency(data.activeLoanAmount)}</span>
                </div>
                <div class="summary-item">
                    <span class="summary-label">Current Outstanding Loan:</span>
                    <span class="summary-val" style="color: ${data.outstandingLoan > 0 ? 'var(--danger)' : 'var(--success)'};">
                        ${formatCurrency(data.outstandingLoan)}
                    </span>
                </div>
            </div>
            <div style="display: flex; gap: 0.75rem; margin-top: 1.25rem;">
                <a href="contributions.html" class="btn btn-secondary btn-sm" style="flex: 1;">💰 Deposit Savings</a>
                <a href="loans.html" class="btn btn-primary btn-sm" style="flex: 1;">📑 Disburse Loan</a>
            </div>
        `;
    } catch (err) {
        console.error(err);
        modalBody.innerHTML = `<div style="color: var(--danger); text-align: center; padding: 1rem;">Failed to load summary: ${err.message}</div>`;
    }
}

function closeSummaryModal() {
    document.getElementById('summary-modal').classList.remove('show');
}
