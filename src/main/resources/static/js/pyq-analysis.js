function getLoggedInUser() {
    const user = JSON.parse(localStorage.getItem("upscMentorUser"));
    if (!user) {
        window.location.href = "/onboarding";
        return null;
    }
    return user;
}

function normalizeMarkdown(input) {
    if (!input) return "";
    let text = String(input).trim();
    if (!text.includes("## ")) {
        text = `## PYQ Analysis\n\n${text}`;
    }
    return text;
}

async function analyzePyq() {
    const user = getLoggedInUser();
    if (!user) return;

    const subject = document.getElementById("pyqSubject").value.trim();
    const topic = document.getElementById("pyqTopic").value.trim();

    if (!subject || !topic) {
        alert("Please provide both subject and topic.");
        return;
    }

    document.getElementById("pyqOutputSection").style.display = "none";
    document.getElementById("pyqLoading").style.display = "block";

    try {
        const response = await fetch("/api/content/pyq-analysis", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                userId: user.id,
                subject,
                topic
            })
        });

        const data = await response.json();
        document.getElementById("pyqLoading").style.display = "none";

        if (data.success) {
            const md = normalizeMarkdown(data.analysis || "");
            document.getElementById("pyqOutput").innerHTML = marked.parse(md, {gfm: true, breaks: true});
            document.getElementById("pyqOutputSection").style.display = "block";
            document.getElementById("pyqOutputSection").scrollIntoView({behavior: "smooth"});
        } else {
            alert("Could not generate PYQ analysis. Please try again.");
        }
    } catch (error) {
        document.getElementById("pyqLoading").style.display = "none";
        alert("Network error while generating PYQ analysis.");
    }
}
