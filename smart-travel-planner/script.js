/*********************************
 * DESTINATION FILTERING LOGIC
 * (Runs on destinations.html)
 *********************************/
function filterSelection(category) {
    const cards = document.getElementsByClassName("filter-item");
    const btns = document.getElementsByClassName("filter-btn");

    // Update Active Button
    for (let btn of btns) {
        btn.classList.remove("active");
        if (btn.innerText.toLowerCase().includes(category) || (category === 'all' && btn.innerText === 'All')) {
            btn.classList.add("active");
        }
    }

    // Show/Hide Cards
    for (let card of cards) {
        if (category === "all") {
            card.style.display = "block";
        } else {
            if (card.getAttribute("data-type") === category) {
                card.style.display = "block";
            } else {
                card.style.display = "none";
            }
        }
    }
}

// Auto-filter based on URL parameters
const params = new URLSearchParams(window.location.search);
if (params.has('type')) {
    // Wait for DOM to load before filtering
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            filterSelection(params.get('type'));
        });
    } else {
        filterSelection(params.get('type'));
    }
}

/*********************************
 * GROUP BUDGET CALCULATION LOGIC
 * (Runs on group.html)
 *********************************/
let totalBudget = 0;
let totalSpent = 0;
let members = 0;

function setBudget() {
    const budgetInput = document.getElementById("budgetInput");
    if (!budgetInput) return; // Safety check

    totalBudget = Number(budgetInput.value);
    document.getElementById("totalBudget").innerText = totalBudget;

    // Set max value for progress bar
    const prog = document.getElementById("budgetProgress");
    if (prog) prog.max = totalBudget;

    calculate();
}

function generateMemberInputs() {
    const countInput = document.getElementById("memberCount");
    if (!countInput) return;

    const count = Number(countInput.value);
    const container = document.getElementById("memberInputs");

    container.innerHTML = "";
    members = count;

    for (let i = 1; i <= count; i++) {
        const input = document.createElement("input");
        input.type = "text";
        input.placeholder = "Member " + i + " Name";
        input.style.marginTop = "10px";
        input.style.width = "100%";
        input.style.padding = "8px";
        container.appendChild(input);
    }

    calculate();
}

function addExpense() {
    const expenseInput = document.getElementById("expenseAmount");
    if (!expenseInput) return;

    const amount = Number(expenseInput.value);
    if (amount > 0) {
        totalSpent += amount;
        document.getElementById("totalSpent").innerText = totalSpent;

        // Update Progress Bar
        const prog = document.getElementById("budgetProgress");
        if (prog) prog.value = totalSpent;

        calculate();
    }
    expenseInput.value = "";
}

function calculate() {
    const remaining = totalBudget - totalSpent;
    const remainingEl = document.getElementById("remaining");

    if (remainingEl) {
        remainingEl.innerText = remaining;
        // Make text red if over budget
        if (remaining < 0) {
            remainingEl.style.color = "red";
            remainingEl.innerText = remaining + " (Over Budget!)";
        } else {
            remainingEl.style.color = "green";
        }
    }

    if (members > 0) {
        const share = Math.floor(totalSpent / members);
        if (document.getElementById("perPerson")) {
            document.getElementById("perPerson").innerText = share;
        }
    }
}