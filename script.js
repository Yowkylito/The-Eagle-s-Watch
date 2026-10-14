(function () {
  var peso = new Intl.NumberFormat("en-PH", {
    style: "currency",
    currency: "PHP",
    maximumFractionDigits: 0
  });

  // Receipt: split the amount across the sample lines
  var slider = document.getElementById("amount");
  var out = document.getElementById("paid-out");
  var rows = document.querySelectorAll("#lines li");

  function render() {
    var total = Number(slider.value);
    out.textContent = peso.format(total);
    rows.forEach(function (row) {
      var pct = Number(row.dataset.pct);
      row.querySelector(".amt").textContent = peso.format(Math.round((total * pct) / 100));
      row.querySelector(".bar-track i").style.setProperty("--w", pct + "%");
    });
  }

  slider.addEventListener("input", render);
  render();

  // Tabs: one panel visible at a time, arrow keys move between tabs
  var tabs = Array.prototype.slice.call(document.querySelectorAll('[role="tab"]'));

  function select(tab) {
    tabs.forEach(function (t) {
      var on = t === tab;
      t.setAttribute("aria-selected", on ? "true" : "false");
      t.tabIndex = on ? 0 : -1;
      document.getElementById(t.getAttribute("aria-controls")).hidden = !on;
    });
  }

  tabs.forEach(function (tab, i) {
    tab.addEventListener("click", function () { select(tab); });
    tab.addEventListener("keydown", function (e) {
      var next;
      if (e.key === "ArrowRight") next = tabs[(i + 1) % tabs.length];
      else if (e.key === "ArrowLeft") next = tabs[(i - 1 + tabs.length) % tabs.length];
      else if (e.key === "Home") next = tabs[0];
      else if (e.key === "End") next = tabs[tabs.length - 1];
      if (next) {
        e.preventDefault();
        select(next);
        next.focus();
      }
    });
  });
})();
