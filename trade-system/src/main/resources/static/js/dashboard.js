const orderForm =
    document.getElementById("orderForm");

const orderMessage =
    document.getElementById("orderMessage");

const ordersBody =
    document.getElementById("ordersBody");

const orderBookBody =
    document.getElementById("orderBookBody");


// =========================================
// STATE
// =========================================

let allOrders = [];

let filteredOrders = [];

let currentPage = 1;

const pageSize = 20;

let sortColumn = "id";

let sortDirection = "desc";


// =========================================
// CREATE ORDER
// =========================================

orderForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();

        const request = {

            symbol:
                document
                    .getElementById("symbol")
                    .value
                    .trim(),

            price:
                Number(
                    document
                        .getElementById("price")
                        .value
                ),

            quantity:
                Number(
                    document
                        .getElementById("quantity")
                        .value
                ),

            side:
                document
                    .getElementById("side")
                    .value
        };


        try {

            const response =
                await fetch(
                    "/api/orders",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(request)
                    }
                );


            if (!response.ok) {

                throw new Error(
                    "Order submission failed"
                );
            }


            const order =
                await response.json();


            orderMessage.textContent =
                `Order ${order.id} created successfully`;

            orderMessage.className =
                "message";


            await refreshOrders();

            await refreshOrderBook(
                request.symbol
            );


            orderForm.reset();

        } catch (error) {

            orderMessage.textContent =
                error.message;

            orderMessage.className =
                "message status-rejected";
        }

    }
);


// =========================================
// LOAD ORDERS
// =========================================

async function refreshOrders() {

    try {

        const response =
            await fetch("/api/orders");


        if (!response.ok) {

            throw new Error(
                "Unable to load orders"
            );
        }


        allOrders =
            await response.json();


        // Newest orders first
        allOrders.sort(
            (a, b) =>
                Number(b.id) - Number(a.id)
        );


        populateFilterOptions();

        updateSummaryCards();

        applyOrderFilters();

    } catch (error) {

        console.error(
            "Order loading failed:",
            error
        );

        ordersBody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">
                    Unable to load orders
                </td>
            </tr>
        `;
    }
}


// =========================================
// FILTER OPTIONS
// =========================================

function populateFilterOptions() {

    const symbolFilter =
        document.getElementById(
            "orderSymbolFilter"
        );

    if (!symbolFilter) {
        return;
    }


    const symbols =
        [...new Set(
            allOrders
                .map(order =>
                    order.symbol
                )
                .filter(Boolean)
        )]
        .sort();


    const currentValue =
        symbolFilter.value;


    symbolFilter.innerHTML = `
        <option value="">All Symbols</option>
    `;


    symbols.forEach(symbol => {

        const option =
            document.createElement("option");

        option.value = symbol;

        option.textContent = symbol;

        symbolFilter.appendChild(option);

    });


    if (
        symbols.includes(
            currentValue
        )
    ) {

        symbolFilter.value =
            currentValue;
    }

}


// =========================================
// APPLY ORDER FILTERS
// =========================================

function applyOrderFilters() {

    const idFilter =
        getValue("orderIdFilter");

    const symbolFilter =
        getValue("orderSymbolFilter");

    const sideFilter =
        getValue("orderSideFilter");

    const statusFilter =
        getValue("orderStatusFilter");

    const minPrice =
        getNumber("orderMinPrice");

    const maxPrice =
        getNumber("orderMaxPrice");

    const minQuantity =
        getNumber("orderMinQuantity");

    const maxQuantity =
        getNumber("orderMaxQuantity");


    filteredOrders =
        allOrders.filter(order => {

            const id =
                String(order.id ?? "");

            const symbol =
                String(order.symbol ?? "")
                    .toUpperCase();

            const side =
                String(order.side ?? "")
                    .toUpperCase();

            const status =
                String(order.status ?? "")
                    .toUpperCase();

            const price =
                Number(order.price);

            const quantity =
                Number(order.quantity);


            if (
                idFilter &&
                !id.includes(idFilter)
            ) {
                return false;
            }


            if (
                symbolFilter &&
                symbol !==
                    symbolFilter.toUpperCase()
            ) {
                return false;
            }


            if (
                sideFilter &&
                side !==
                    sideFilter.toUpperCase()
            ) {
                return false;
            }


            if (
                statusFilter &&
                status !==
                    statusFilter.toUpperCase()
            ) {
                return false;
            }


            if (
                minPrice !== null &&
                price < minPrice
            ) {
                return false;
            }


            if (
                maxPrice !== null &&
                price > maxPrice
            ) {
                return false;
            }


            if (
                minQuantity !== null &&
                quantity < minQuantity
            ) {
                return false;
            }


            if (
                maxQuantity !== null &&
                quantity > maxQuantity
            ) {
                return false;
            }


            return true;

        });


    currentPage = 1;

    sortOrders();

    renderOrders();

    renderPagination();

}


// =========================================
// SORT
// =========================================

function sortOrders() {

    filteredOrders.sort(
        (a, b) => {

            let valueA =
                a[sortColumn];

            let valueB =
                b[sortColumn];


            if (
                sortColumn === "id" ||
                sortColumn === "price" ||
                sortColumn === "quantity"
            ) {

                valueA =
                    Number(valueA);

                valueB =
                    Number(valueB);

            } else {

                valueA =
                    String(
                        valueA ?? ""
                    ).toUpperCase();

                valueB =
                    String(
                        valueB ?? ""
                    ).toUpperCase();
            }


            if (valueA < valueB) {

                return sortDirection === "asc"
                    ? -1
                    : 1;
            }


            if (valueA > valueB) {

                return sortDirection === "asc"
                    ? 1
                    : -1;
            }


            return 0;

        }
    );

}


// =========================================
// RENDER ORDERS
// =========================================

function renderOrders() {

    ordersBody.innerHTML = "";


    if (
        filteredOrders.length === 0
    ) {

        ordersBody.innerHTML = `
            <tr>
                <td colspan="6"
                    class="empty-state">
                    No orders match the selected filters
                </td>
            </tr>
        `;

        return;
    }


    const startIndex =
        (currentPage - 1) *
        pageSize;


    const pageOrders =
        filteredOrders.slice(
            startIndex,
            startIndex + pageSize
        );


    pageOrders.forEach(order => {

        const row =
            document.createElement("tr");


        row.innerHTML = `

            <td>${escapeHtml(order.id)}</td>

            <td>${escapeHtml(order.symbol)}</td>

            <td>${formatNumber(order.price)}</td>

            <td>${formatNumber(order.quantity)}</td>

            <td>${escapeHtml(order.side)}</td>

            <td class="${getStatusClass(order.status)}">
                ${escapeHtml(order.status)}
            </td>

        `;


        ordersBody.appendChild(row);

    });

}


// =========================================
// PAGINATION
// =========================================

function renderPagination() {

    const pagination =
        document.getElementById(
            "ordersPagination"
        );


    if (!pagination) {
        return;
    }


    pagination.innerHTML = "";


    const totalPages =
        Math.ceil(
            filteredOrders.length /
            pageSize
        );


    if (totalPages <= 1) {

        pagination.innerHTML = `
            <span>
                Showing ${filteredOrders.length}
                of ${allOrders.length} orders
            </span>
        `;

        return;
    }


    const start =
        ((currentPage - 1) * pageSize) + 1;

    const end =
        Math.min(
            currentPage * pageSize,
            filteredOrders.length
        );


    const info =
        document.createElement("span");

    info.className =
        "pagination-info";

    info.textContent =
        `Showing ${start}-${end} of ${filteredOrders.length}`;


    const previous =
        document.createElement("button");

    previous.textContent =
        "Previous";

    previous.disabled =
        currentPage === 1;

    previous.addEventListener(
        "click",
        function () {

            currentPage--;

            renderOrders();

            renderPagination();
        }
    );


    const next =
        document.createElement("button");

    next.textContent =
        "Next";

    next.disabled =
        currentPage === totalPages;

    next.addEventListener(
        "click",
        function () {

            currentPage++;

            renderOrders();

            renderPagination();
        }
    );


    pagination.appendChild(info);

    pagination.appendChild(previous);

    pagination.appendChild(next);

}


// =========================================
// SUMMARY CARDS
// =========================================

function updateSummaryCards() {

    const total =
        allOrders.length;


    const filled =
        allOrders.filter(
            order =>
                order.status === "FILLED"
        ).length;


    const partiallyFilled =
        allOrders.filter(
            order =>
                order.status ===
                "PARTIALLY_FILLED"
        ).length;


    const rejected =
        allOrders.filter(
            order =>
                order.status === "REJECTED"
        ).length;


    const active =
        allOrders.filter(
            order =>
                [
                    "NEW",
                    "VALIDATING",
                    "ACCEPTED",
                    "SENT_TO_EXCHANGE",
                    "PARTIALLY_FILLED"
                ].includes(
                    order.status
                )
        ).length;


    setText(
        "totalOrders",
        total
    );

    setText(
        "activeOrders",
        active
    );

    setText(
        "filledOrders",
        filled
    );

    setText(
        "partialOrders",
        partiallyFilled
    );

    setText(
        "rejectedOrders",
        rejected
    );


    updateStatusChart(
        filled,
        active,
        rejected,
        partiallyFilled
    );

}


// =========================================
// ORDER BOOK
// =========================================

async function refreshOrderBook(symbol) {

    if (!symbol) {

        orderBookBody.innerHTML = `
            <tr>
                <td colspan="3"
                    class="empty-state">
                    Enter a symbol to view the order book
                </td>
            </tr>
        `;

        return;
    }


    try {

        const response =
            await fetch(
                `/api/order-book?symbol=${encodeURIComponent(symbol)}`
            );


        if (!response.ok) {

            throw new Error(
                "Unable to load order book"
            );
        }


        const levels =
            await response.json();


        renderOrderBook(levels);

    } catch (error) {

        console.error(
            "Order book loading failed:",
            error
        );

        orderBookBody.innerHTML = `
            <tr>
                <td colspan="3"
                    class="empty-state">
                    Unable to load order book
                </td>
            </tr>
        `;
    }

}


// =========================================
// RENDER ORDER BOOK
// =========================================

function renderOrderBook(levels) {

    const sideFilter =
        getValue(
            "orderBookSideFilter"
        );


    const minPrice =
        getNumber(
            "orderBookMinPrice"
        );


    const maxPrice =
        getNumber(
            "orderBookMaxPrice"
        );


    const filteredLevels =
        levels.filter(level => {

            const price =
                Number(level.price);

            const buyQty =
                Number(
                    level.buyQuantity ?? 0
                );

            const sellQty =
                Number(
                    level.sellQuantity ?? 0
                );


            if (
                minPrice !== null &&
                price < minPrice
            ) {
                return false;
            }


            if (
                maxPrice !== null &&
                price > maxPrice
            ) {
                return false;
            }


            if (
                sideFilter === "BUY" &&
                buyQty <= 0
            ) {
                return false;
            }


            if (
                sideFilter === "SELL" &&
                sellQty <= 0
            ) {
                return false;
            }


            return true;

        });


    orderBookBody.innerHTML = "";


    if (
        filteredLevels.length === 0
    ) {

        orderBookBody.innerHTML = `
            <tr>
                <td colspan="3"
                    class="empty-state">
                    No matching order-book levels
                </td>
            </tr>
        `;

        return;
    }


    filteredLevels.forEach(level => {

        const row =
            document.createElement("tr");


        row.innerHTML = `

            <td>${formatNumber(level.price)}</td>

            <td>${formatNumber(level.buyQuantity ?? 0)}</td>

            <td>${formatNumber(level.sellQuantity ?? 0)}</td>

        `;


        orderBookBody.appendChild(row);

    });

}


// =========================================
// STATUS CSS
// =========================================

function getStatusClass(status) {

    switch (status) {

        case "FILLED":
            return "status-filled";

        case "PARTIALLY_FILLED":
            return "status-partial";

        case "ACCEPTED":
            return "status-accepted";

        case "REJECTED":
            return "status-rejected";

        default:
            return "";

    }

}


// =========================================
// SORT HEADER EVENTS
// =========================================

document
    .querySelectorAll(
        "#ordersTable th[data-sort]"
    )
    .forEach(header => {

        header.addEventListener(
            "click",
            function () {

                const column =
                    this.dataset.sort;


                if (
                    sortColumn === column
                ) {

                    sortDirection =
                        sortDirection === "asc"
                            ? "desc"
                            : "asc";

                } else {

                    sortColumn =
                        column;

                    sortDirection =
                        "asc";
                }


                sortOrders();

                renderOrders();

            }
        );

    });


// =========================================
// FILTER EVENTS
// =========================================

[
    "orderIdFilter",
    "orderSymbolFilter",
    "orderSideFilter",
    "orderStatusFilter",
    "orderMinPrice",
    "orderMaxPrice",
    "orderMinQuantity",
    "orderMaxQuantity"
]
.forEach(id => {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.addEventListener(
        "input",
        applyOrderFilters
    );


    element.addEventListener(
        "change",
        applyOrderFilters
    );

});


// =========================================
// CLEAR FILTERS
// =========================================

const clearFilters =
    document.getElementById(
        "clearOrderFilters"
    );


if (clearFilters) {

    clearFilters.addEventListener(
        "click",
        function () {

            [
                "orderIdFilter",
                "orderSymbolFilter",
                "orderSideFilter",
                "orderStatusFilter",
                "orderMinPrice",
                "orderMaxPrice",
                "orderMinQuantity",
                "orderMaxQuantity"
            ]
            .forEach(id => {

                const element =
                    document.getElementById(id);

                if (element) {
                    element.value = "";
                }

            });


            applyOrderFilters();

        }
    );

}


// =========================================
// ORDER BOOK FILTER EVENTS
// =========================================

[
    "orderBookSideFilter",
    "orderBookMinPrice",
    "orderBookMaxPrice"
]
.forEach(id => {

    const element =
        document.getElementById(id);


    if (!element) {
        return;
    }


    element.addEventListener(
        "input",
        refreshCurrentOrderBook
    );


    element.addEventListener(
        "change",
        refreshCurrentOrderBook
    );

});


async function refreshCurrentOrderBook() {

    const symbol =
        document
            .getElementById("bookSymbol")
            ?.value
            .trim();


    await refreshOrderBook(symbol);

}


// =========================================
// REFRESH BUTTONS
// =========================================

document
    .getElementById("refreshOrders")
    .addEventListener(
        "click",
        refreshOrders
    );


document
    .getElementById("refreshBook")
    .addEventListener(
        "click",
        refreshCurrentOrderBook
    );


// =========================================
// UTILITY FUNCTIONS
// =========================================

function getValue(id) {

    const element =
        document.getElementById(id);

    return element
        ? element.value.trim()
        : "";

}


function getNumber(id) {

    const value =
        getValue(id);


    if (value === "") {
        return null;
    }


    const number =
        Number(value);


    return Number.isFinite(number)
        ? number
        : null;

}


function setText(id, value) {

    const element =
        document.getElementById(id);


    if (element) {

        element.textContent =
            value;
    }

}


function formatNumber(value) {

    if (
        value === null ||
        value === undefined ||
        value === ""
    ) {
        return "0";
    }


    return Number(value).toLocaleString(
        "en-IN"
    );

}


function escapeHtml(value) {

    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

}


// =========================================
// LIGHTWEIGHT STATUS CHART
// =========================================

function updateStatusChart(
    filled,
    active,
    rejected,
    partial
) {

    const chart =
        document.getElementById(
            "statusChart"
        );


    if (!chart) {
        return;
    }


    const total =
        filled +
        active +
        rejected;


    if (total === 0) {

        chart.style.background =
            "none";

        return;
    }


    const filledPercent =
        (filled / total) * 100;

    const activePercent =
        (active / total) * 100;


    const rejectedPercent =
        (rejected / total) * 100;


    chart.style.background = `
        conic-gradient(
            #22c55e 0% ${filledPercent}%,
            #3b82f6 ${filledPercent}%
                ${filledPercent + activePercent}%,
            #ef4444 ${filledPercent + activePercent}%
                100%
        )
    `;

}


// =========================================
// INITIAL LOAD
// =========================================

refreshOrders();

refreshOrderBook("INFY");