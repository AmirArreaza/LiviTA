const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/services');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    var li = document.createElement("li");
    var link = document.createElement("a");
    link.setAttribute('href', "javascript:deleteService('" + service.name + "');");
    link.textContent = "Delete?"
    li.appendChild(document.createTextNode(service.name + ': ' + service.status));
    li.appendChild(link);
    listContainer.appendChild(li);
  });
});

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let serviceName = document.querySelector('#service-name').value;
    let serviceUrl = document.querySelector('#service-url').value;
    fetch('/services', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name:serviceName,url:serviceUrl})
    }).then(res=> location.reload());
}

function deleteService(service) {
  fetch('/services/' + service, {
          method: 'delete',
          headers: {
              'Accept': 'application/json, text/plain, */*',
              'Content-Type': 'application/json'
          },
          body: ""
      }).then(res=> location.reload());
}