let aux = document.location.href.split('?query=');
let aux1 = aux[1].split('&');
let query = aux1[0];
let txtSearch = document.getElementById("searchInput");

txtSearch.addEventListener("keypress", function(event){
  if (event.key === "Enter") {
    search()
  }
});

txtSearch.value = decodeURI(query);

let auxp = document.location.href.split('&page=');
let auxp1 = auxp[1].split('&');
let page = auxp1[0];
let intPage = 0;
if (!isNaN(page)) {
  intPage = parseInt(page) + 1;
  document.getElementById("page").innerHTML = `Page ${intPage}`;
}

let auxs = document.location.href.split('&size=');
let size = 20;
if (auxs.length > 1) {
  let auxs1 = auxs[1].split('&');
  size = parseInt(auxs1[0]);
}


fetch(`http://localhost:8080/api/search?query=${query}&page=${intPage}&size=${size}`)
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
  let query = txtSearch.value;
  query = query.trim();
  query = query.replaceAll(/\s+/g, ' ');
  if (query != "") {
    let auxs = document.location.href.split('&size=');
    let size = 'a';
    if (auxs.length > 1) {
      let auxs1 = auxs[1].split('&');
      size = auxs1[0];
    }
    if (!isNaN(size)) {
      document.location.href = 'results.html?query=' + query + '&page=0&size=' + size;
    } else {
      document.location.href = 'results.html?query=' + query + '&page=0';
    }
  }
}

function nextPage(){
  let auxp = document.location.href.split('&page=');
  let auxp1 = auxp[1].split('&');
  let page = auxp1[0];
  if (!isNaN(page)) {
    let intPage = parseInt(page) + 1;
    let auxq = document.location.href.split('?query=');
    let auxq1 = auxq[1].split('&');
    let query = auxq1[0];
    let auxs = document.location.href.split('&size=');
    let size = 'a';
    if (auxs.length > 1) {
      let auxs1 = auxs[1].split('&');
      size = auxs1[0];
    }
    if (!isNaN(size)) {
      document.location.href = 'results.html?query=' + query + '&page=' + intPage + '&size=' + size;
    } else {
      document.location.href = 'results.html?query=' + query + '&page=' + intPage;
    }
  }
}

function indx() {
  fetch(`http://localhost:8080/api/indx`)
  .then((response) => {
    if (response.ok) {
      alert("Indexing process started");
    }else{
      throw new Error("Something went wrong");
    }
  })
  .catch((error) => {
    console.log(error)
    alert("Error: " + error.message);
  })
}

function stpindx() {
  fetch(`http://localhost:8080/api/stpindx`)
  .then((response) => {
    if (response.ok) {
      alert("Indexing process stopped");
    }else{
      throw new Error("Something went wrong");
    }
  })
  .catch((error) => {
    console.log(error)
    alert("Error: " + error.message);
  })
}


let urlsToIndex = document.getElementById("urlsToIndex");


urlsToIndex.addEventListener("keypress", function(event){
  if (event.key === "Enter") {
    sendurls()
  }
});

function sendurls() {
  let urls = urlsToIndex.value;
  urls = urls.trim();
  urls = urls.replaceAll(/\s+/g, ' ');
  let urlsArray = urls.split(' ');
  let jsonPayload = {
  urls: urlsArray
  };

  if (urls != "") {
    fetch(`http://localhost:8080/api/addurls`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(jsonPayload),
    })
    .then((response) => {
      if (response.ok) {
        alert("URLs have been sent")
        urlsToIndex.value = "";
      }
      return response.json();
    })
    .then(jsonResponse => {
      if (jsonResponse.error) {
        throw new Error("Status Code: "+jsonResponse.status+". "+jsonResponse.message);
      }
    })
    .catch(error => {
      console.log(error)
      alert(error.message);
    });
  }
}