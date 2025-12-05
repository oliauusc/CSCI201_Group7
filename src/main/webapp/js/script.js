function sendForm() {
    const form = document.getElementById("myForm");
    const formData = new FormData(form);

    // Build query string manually
    const params = new URLSearchParams();
    for (let [key, value] of formData.entries()) {
        params.append(key, value);
    }

    const xhr = new XMLHttpRequest();
    xhr.open("GET", "validation?" + params.toString(), true);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {

            const json = JSON.parse(xhr.responseText);

            let table = "<table border='1'>";
            for (let key in json) {
                table += `<tr><td>${key}</td><td>${json[key]}</td></tr>`;
            }
            table += "</table>";

            document.getElementById("result").innerHTML = table;
        }
    };

    xhr.send();
    return false; // prevents normal form submission
}
