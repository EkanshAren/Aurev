
/* =========================================
   AUREV — INTERACTIONS & MOTION
========================================= */

document.addEventListener("DOMContentLoaded", () => {
  const dashboardBtn = document.getElementById("dashboardBtn");
  const dashboardView = document.getElementById("dashboardView");

  // Dashboard slides in from the left when clicked
  if (dashboardBtn && dashboardView) {
    dashboardBtn.addEventListener("click", () => {
      dashboardView.classList.remove("is-entering");

      // Restart animation
      void dashboardView.offsetWidth;

      dashboardView.classList.add("is-entering");
      dashboardBtn.classList.add("active");

      // Remove active state from other sidebar items
      document.querySelectorAll(".sidebar .nav-item").forEach(item => {
        if (item !== dashboardBtn) {
          item.classList.remove("active");
        }
      });
    });
  }

  // Reveal cards and sections as they enter the screen
  const revealElements = document.querySelectorAll(
    ".kpi-card, .upload-card, .card, .panel, .content-card"
  );

  revealElements.forEach((element, index) => {
    element.classList.add("reveal");
    element.style.transitionDelay = `${Math.min(index * 70, 350)}ms`;
  });

  if ("IntersectionObserver" in window) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add("visible");
          observer.unobserve(entry.target);
        }
      });
    }, {
      threshold: 0.12
    });

    revealElements.forEach(element => observer.observe(element));
  } else {
    revealElements.forEach(element => element.classList.add("visible"));
  }

  // Add hover feedback to buttons
  document.querySelectorAll("button, .btn").forEach(button => {
    button.addEventListener("mouseenter", () => {
      button.classList.add("is-hovered");
    });

    button.addEventListener("mouseleave", () => {
      button.classList.remove("is-hovered");
    });
  });

  // Animate table rows when they appear
  const tableBody = document.getElementById("reconciliationRows");

  if (tableBody) {
    const rowObserver = new MutationObserver(() => {
      [...tableBody.querySelectorAll("tr")].forEach((row, index) => {
        row.animate(
          [
            { opacity: 0, transform: "translateY(9px)" },
            { opacity: 1, transform: "translateY(0)" }
          ],
          {
            duration: 380,
            delay: index * 65,
            easing: "ease-out",
            fill: "both"
          }
        );
      });
    });

    rowObserver.observe(tableBody, {
      childList: true
    });
  }

  // Subtle glow follows the pointer over cards
  document.querySelectorAll(
    ".kpi-card, .upload-card, .card, .panel"
  ).forEach(card => {
    card.addEventListener("pointermove", event => {
      const rect = card.getBoundingClientRect();

      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;

      card.style.setProperty("--mouse-x", `${x}px`);
      card.style.setProperty("--mouse-y", `${y}px`);
    });
  });

  // Add a premium selected state to upload inputs
  ["noticePdf", "gstr2bJson", "purchaseJson"].forEach(id => {
    const input = document.getElementById(id);

    if (!input) return;

    input.addEventListener("change", () => {
      const card = input.closest(
        ".upload-card, .upload-zone, .file-upload, .card"
      );

      if (card && input.files && input.files.length > 0) {
        card.classList.add("file-selected");
      }
    });
  });
});
