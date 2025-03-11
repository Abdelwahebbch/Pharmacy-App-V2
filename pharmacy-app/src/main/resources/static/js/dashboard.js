// Simple dashboard functionality
document.addEventListener("DOMContentLoaded", () => {
  // You can add chart initialization here if needed
  console.log("Dashboard loaded successfully")

  // Example: Add event listeners to sidebar links
  const sidebarLinks = document.querySelectorAll("#sidebar .nav-link")
  sidebarLinks.forEach((link) => {
    link.addEventListener("click", function (e) {
      // Remove active class from all links
      sidebarLinks.forEach((l) => l.classList.remove("active"))
      // Add active class to clicked link
      this.classList.add("active")
    })
  })
})

