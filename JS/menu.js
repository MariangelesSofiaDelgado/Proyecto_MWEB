const menuData = {
    piqueos: {
        title: "Piqueos & Entradas",
        description: "Ideales para compartir mientras disfrutas del sabor marino.",
        items: [
            { id: "piqueo-ceviche", name: "Ceviche clásico", description: "Pescado del día, leche de tigre cítrica y cancha crocante.", price: 28 },
            { id: "piqueo-choros", name: "Choros a la chalaca", description: "Mejillones frescos con cebolla, tomate y limón.", price: 22 },
            { id: "piqueo-tiradito", name: "Tiradito de ají amarillo", description: "Láminas de pescado con crema suave de ají amarillo.", price: 30 }
        ]
    },
    sandwiches: {
        title: "Sándwiches",
        description: "Opciones marinas con panes artesanales y salsas frescas.",
        items: [
            { id: "sandwich-pejerrey", name: "Pan con pejerrey", description: "Pejerrey crocante, salsa tártara y limón.", price: 20 },
            { id: "sandwich-pescado", name: "Pan con pescado", description: "Filete dorado con criolla y mayo cítrica.", price: 24 },
            { id: "sandwich-calamares", name: "Pan con calamares", description: "Calamares rebozados con alioli de limón.", price: 26 }
        ]
    },
    fondos: {
        title: "Platos de fondo",
        description: "Platos principales con el toque de la casa.",
        items: [
            { id: "fondo-arroz-mariscos", name: "Arroz con mariscos", description: "Arroz meloso con mix de mariscos y sofrito peruano.", price: 38 },
            { id: "fondo-parihuela", name: "Parihuela", description: "Sopa concentrada de mariscos con ají y hierbas.", price: 36 },
            { id: "fondo-chaufa", name: "Chaufa de mariscos", description: "Salteado al wok con arroz, mariscos y salsa oriental.", price: 34 }
        ]
    },
    postres: {
        title: "Postres",
        description: "Dulces ligeros para cerrar tu experiencia.",
        items: [
            { id: "postre-suspiro", name: "Suspiro limeño", description: "Crema de leche y merengue con canela.", price: 16 },
            { id: "postre-pie-limon", name: "Pie de limón", description: "Base crocante con crema cítrica y merengue.", price: 15 },
            { id: "postre-cheesecake", name: "Cheesecake de maracuyá", description: "Cheesecake suave con salsa de maracuyá.", price: 17 }
        ]
    },
    bebidas: {
        title: "Bebidas",
        description: "Refrescantes y perfectas para acompañar tu pedido.",
        items: [
            { id: "bebida-chicha", name: "Chicha morada", description: "Bebida tradicional con frutas y especias.", price: 10 },
            { id: "bebida-limonada", name: "Limonada hierbabuena", description: "Limonada fresca con toque de hierbabuena.", price: 11 },
            { id: "bebida-maracuya", name: "Jugo de maracuyá", description: "Jugo natural de maracuyá.", price: 12 }
        ]
    }
};

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

updateCartUI();