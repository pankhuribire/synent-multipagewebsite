if (contactForm) {

    contactForm.addEventListener("submit", function (event) {

        event.preventDefault();

        const name =
            document.getElementById("name").value.trim();

        const email =
            document.getElementById("email").value.trim();

        const subject =
            document.getElementById("subject").value.trim();

        const message =
            document.getElementById("message").value.trim();

        if (
            name === "" ||
            email === "" ||
            subject === "" ||
            message === ""
        ) {

            formMessage.innerText =
                "Please fill in all the fields.";

            return;
        }

        const formData = new URLSearchParams();

        formData.append("name", name);
        formData.append("email", email);
        formData.append("subject", subject);
        formData.append("message", message);

        formMessage.innerText =
            "Sending message...";

        fetch(
            "http://localhost:8081/contact",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body: formData
            }
        )

        .then(response => {

            if (!response.ok) {

                throw new Error(
                    "Failed to submit message"
                );
            }

            return response.json();
        })

        .then(data => {

            formMessage.innerText =
                data.message;

            contactForm.reset();
        })

        .catch(error => {

            formMessage.innerText =
                "Unable to connect to the server.";

            console.error(error);
        });

    });

}