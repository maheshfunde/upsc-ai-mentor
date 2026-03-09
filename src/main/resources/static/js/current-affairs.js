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
        text = `## Analysis\n\n${text}`;
    }
    return text;
}

async function analyzeCurrentAffairs() {
    const user = getLoggedInUser();
    if (!user) return;

    const articleText = document.getElementById("caContent").value.trim();

    if (!articleText) {
        alert("Please paste article/news content.");
        return;
    }

    document.getElementById("caOutputSection").style.display = "none";
    document.getElementById("caLoading").style.display = "block";

    try {
        const response = await fetch("/api/content/current-affairs", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                userId: user.id,
                articleText
            })
        });

        const data = await response.json();
        document.getElementById("caLoading").style.display = "none";

        if (data.success) {
            const md = normalizeMarkdown(data.analysis || "");
            document.getElementById("caOutput").innerHTML = marked.parse(md, {gfm: true, breaks: true});
            document.getElementById("caOutputSection").style.display = "block";
            document.getElementById("caOutputSection").scrollIntoView({behavior: "smooth"});
        } else {
            alert("Could not analyze content. Please try again.");
        }
    } catch (error) {
        document.getElementById("caLoading").style.display = "none";
        alert("Network error while analyzing content.");
    }
}
