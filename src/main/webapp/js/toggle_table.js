function toggleTable(id) {
    const el = document.getElementById(id);
    el.style.display = el.style.display === "none" ? "" : "none";
}

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".toggle-table").forEach((toggle) => {
        toggle.addEventListener("click", (event) => {
            event.preventDefault();
            const { toggleTarget } = event.target.dataset;

            toggleTable(toggleTarget);
        });
    });
});
