// agregar conexion con la API de productos aqui: cargar el menu real desde Spring Boot
// cuando no haya backend, el menu queda vacio
const menuData = {};

// agregar conexion con backend aqui: cargar menu dinamico desde la base de datos
// const menuData = await fetch("/api/productos").then((response) => response.json());

const cart = new Map();
const formatter = new Intl.NumberFormat("es-PE", {
    style: "currency",
    currency: "PEN",
    minimumFractionDigits: 2
});

const menuModalElement = document.getElementById("menuModal");
const menuItemsContainer = document.getElementById("menuItems");
const cartItemsContainer = document.getElementById("cartItems");
const cartTotalElement = document.getElementById("cartTotal");
const menuModalLabel = document.getElementById("menuModalLabel");
const menuModalDescription = document.getElementById("menuModalDescription");
const submitOrderButton = document.getElementById("submitOrderButton");

const menuModal = menuModalElement ? new bootstrap.Modal(menuModalElement) : null;

const updateCartUI = () => {
    cartItemsContainer.innerHTML = "";

    if (cart.size === 0) {
        const emptyItem = document.createElement("li");
        emptyItem.className = "text-muted";
        emptyItem.textContent = "Aún no agregas platos.";
        cartItemsContainer.appendChild(emptyItem);
    } else {
        cart.forEach((item) => {
            const listItem = document.createElement("li");
            listItem.className = "d-flex justify-content-between align-items-center mb-2";
            listItem.innerHTML = `
                <span>${item.name}</span>
                <strong class="menu-price">${formatter.format(item.price)}</strong>
            `;
            cartItemsContainer.appendChild(listItem);
        });
    }

    const total = Array.from(cart.values()).reduce((sum, item) => sum + item.price, 0);
    cartTotalElement.textContent = formatter.format(total);
};

const renderMenuItems = (categoryKey) => {
    const category = menuData[categoryKey];
    if (!category) return;

    menuModalLabel.textContent = category.title;
    menuModalDescription.textContent = category.description;
    menuItemsContainer.innerHTML = "";

    category.items.forEach((item) => {
        const wrapper = document.createElement("label");
        wrapper.className = "menu-item";
        wrapper.innerHTML = `
            <div>
                <h6>${item.name}</h6>
                <p>${item.description}</p>
            </div>
            <div class="d-flex align-items-center gap-3">
                <span class="menu-price">${formatter.format(item.price)}</span>
                <input class="form-check-input" type="checkbox" data-item-id="${item.id}">
            </div>
        `;
        const checkbox = wrapper.querySelector("input");
        checkbox.checked = cart.has(item.id);
        checkbox.addEventListener("change", (event) => {
            if (event.target.checked) {
                cart.set(item.id, item);
            } else {
                cart.delete(item.id);
            }
            updateCartUI();
        });
        menuItemsContainer.appendChild(wrapper);
    });

    menuModal.show();
};

const categoryButtons = document.querySelectorAll(".categoria-btn");
categoryButtons.forEach((button) => {
    button.addEventListener("click", () => {
        const categoryKey = button.dataset.category;
        renderMenuItems(categoryKey);
    });
});

const buildOrderPayload = () => ({
    // agregar conexion con backend aqui: incluir datos reales como mesa_id, cliente_id o notas del pedido
    items: Array.from(cart.values()).map((item) => ({
        id: item.id,
        name: item.name,
        price: item.price
    })),
    total: Array.from(cart.values()).reduce((sum, item) => sum + item.price, 0)
});

const submitOrder = async () => {
    if (cart.size === 0) {
        alert("Agrega al menos un plato antes de enviar el pedido.");
        return;
    }

    const payload = buildOrderPayload();

    try {
        // agregar conexion con backend aqui: enviar pedido a Spring Boot
        // await fetch("/api/pedidos", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
        // agregar conexion con backend aqui: manejar respuesta (id de pedido, estado y tiempo estimado)
        console.log("Pedido listo para enviar:", payload);
        alert("Pedido listo para enviar. (Pendiente integración backend)");
    } catch (error) {
        console.error("Error al enviar pedido", error);
        alert("No se pudo enviar el pedido. Intenta nuevamente.");
    }
};

if (submitOrderButton) {
    submitOrderButton.addEventListener("click", submitOrder);
}

updateCartUI();
