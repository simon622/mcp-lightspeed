function showToast(message, type = "success", timeout = 4000) {
    const container = document.getElementById("toast-container");

    // pick style
    const baseStyle =
        "px-6 py-3 rounded-xl shadow-2xl text-white font-semibold text-lg flex items-center space-x-3 transform transition-all duration-500 opacity-0 translate-y-4";
    const style =
        type === "error"
            ? "bg-red-600"
            : "bg-green-600"; // success default

    // create element
    const toast = document.createElement("div");
    toast.className = `${baseStyle} ${style}`;
    toast.innerHTML = `
    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 flex-shrink-0 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
    </svg>
    <span>${message}</span>
  `;

    container.appendChild(toast);

    // animate in
    requestAnimationFrame(() => {
        toast.classList.remove("opacity-0", "translate-y-4");
        toast.classList.add("opacity-100", "translate-y-0");
    });

    // animate out
    setTimeout(() => {
        toast.classList.remove("opacity-100", "translate-y-0");
        toast.classList.add("opacity-0", "translate-y-4");
        setTimeout(() => toast.remove(), 500);
    }, timeout);
}
