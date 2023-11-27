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
    document.location.href = 'results.html?query=' + textsearch + '&page=0' + '&size=20';
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