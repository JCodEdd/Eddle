let searchInput = document.getElementById('searchInput');

searchInput.addEventListener("keypress", function(event){
  if (event.key === "Enter") {
    search()
  }
});

function search(){
  let textsearch = searchInput.value;
  textsearch = textsearch.trim();
  textsearch = textsearch.replaceAll(/\s+/g, ' ');
  if (textsearch != "") {
    document.location.href = 'results.html?query=' + textsearch;
  }
}

function indx() {
  fetch(`http://localhost:8080/api/indx`)
    .then(response => {});
}

function stpindx() {
  fetch(`http://localhost:8080/api/stpindx`)
    .then(response => {});
}