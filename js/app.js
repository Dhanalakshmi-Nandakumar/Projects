function startReview() {

    alert("Let's begin our little journey ❤️");

}


let selectedRating = 0;


document.querySelectorAll(".love-rating button").forEach(button => {

    button.addEventListener("click", function () {

        selectedRating = this.dataset.rating;

        console.log("Selected rating:", selectedRating);

    });

});


function submitReview() {

    const relationshipMeaning =
        document.getElementById("relationshipMeaning").value;

    const favoriteThing =
        document.getElementById("favoriteThing").value;

    const improvement =
        document.getElementById("improvement").value;


    const review = {

        relationshipMeaning: relationshipMeaning,

        loveRating: selectedRating,

        favoriteThing: favoriteThing,

        improvement: improvement

    };


    console.log("Sending review:", review);


    fetch("http://localhost:8080/api/reviews", {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(review)

    })
    .then(response => response.text())
    .then(data => {

        console.log("Backend response:", data);

    })
    .catch(error => {

        console.error("Error sending review:", error);

    });
}