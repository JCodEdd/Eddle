let aux = document.location.href.split('?query=');
let query = aux[1];
let txtSearch = document.getElementById("searchInput");

txtSearch.addEventListener("keypress", function(event){
  if (event.key === "Enter") {
    search()
  }
});

txtSearch.value = decodeURI(query);

fetch(`http://localhost:8080/api/search?query=${query}`)
.then(response => response.json())
.then(json => {
  let html = '';
  for(let resultSearch of json){
    html += getHtmlResultSearch(resultSearch)
  }
  document.getElementById("links").outerHTML = html;
});

function getHtmlResultSearch(resultSearch) {
  return `<div class="results">
    <h3><a href="${resultSearch.url}" target="_blank"> ${resultSearch.title} </a></h3>
    <span> ${resultSearch.description}</span>
  </div>`;
}

function search(){
  query = txtSearch.value;
  query = query.trim();
  query = query.replaceAll(/\s+/g, ' ');
  if (query != "") {
    document.location.href = 'results.html?query=' + query;
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