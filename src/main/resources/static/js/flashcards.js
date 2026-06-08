// ============================================
// UPSC AI MENTOR - FLASHCARDS LOGIC
// ============================================

let currentDeck = [];
let currentIndex = 0;
let isFlipped = false;

// Flashcard data organized by topic
const flashcardDecks = {
    articles: [
        { q: "Article 12", a: "Definition of 'State' — includes Government, Parliament, State Legislatures, and all local authorities within India" },
        { q: "Article 13", a: "Laws inconsistent with Fundamental Rights are void — basis of Judicial Review" },
        { q: "Article 14", a: "Equality before law and equal protection of laws — applies to ALL persons (citizens + foreigners)" },
        { q: "Article 15", a: "Prohibition of discrimination on grounds of religion, race, caste, sex, or place of birth" },
        { q: "Article 16", a: "Equality of opportunity in public employment — permits reservation for SC/ST/OBC" },
        { q: "Article 17", a: "Abolition of Untouchability — absolute right, no exceptions, enforceable by law" },
        { q: "Article 19", a: "Six freedoms: Speech, Assembly, Association, Movement, Residence, Profession (citizens only)" },
        { q: "Article 21", a: "Protection of life and personal liberty — 'heart of Fundamental Rights', includes right to privacy, education, health, environment" },
        { q: "Article 21A", a: "Right to Education — free and compulsory education for children aged 6-14 (86th Amendment, 2002)" },
        { q: "Article 32", a: "Constitutional Remedies — Ambedkar called it 'heart and soul of the Constitution'. 5 writs: Habeas Corpus, Mandamus, Prohibition, Certiorari, Quo Warranto" },
        { q: "Article 226", a: "High Court writ jurisdiction — wider than Article 32 (covers both Fundamental Rights AND legal rights)" },
        { q: "Article 356", a: "President's Rule — failure of constitutional machinery in a State (S.R. Bommai case laid down guidelines)" },
        { q: "Article 368", a: "Power of Parliament to amend the Constitution — requires special majority (and state ratification for some provisions)" },
        { q: "Article 370", a: "Special status to Jammu & Kashmir — abrogated in 2019, replaced by J&K Reorganisation Act" },
    ],
    schemes: [
        { q: "MGNREGA", a: "Mahatma Gandhi National Rural Employment Guarantee Act, 2005 — 100 days wage employment to rural households. Right to Work legislation" },
        { q: "PM Jan Dhan Yojana", a: "2014 — Financial inclusion: zero balance bank accounts, RuPay debit cards, insurance cover. Foundation for DBT" },
        { q: "Ayushman Bharat (PM-JAY)", a: "2018 — Health insurance of ₹5 lakh/family/year for secondary & tertiary hospitalization. World's largest government-funded healthcare programme" },
        { q: "Swachh Bharat Mission", a: "2014 — Clean India: elimination of open defecation (ODF), solid waste management. Phase-II focuses on sustainability" },
        { q: "PM Awas Yojana", a: "2015 — Housing for All: affordable housing for urban and rural poor. Target: 2 crore houses by 2024" },
        { q: "Make in India & PLI", a: "2014 — Boost domestic manufacturing. PLI (Production Linked Incentive) schemes across 14 sectors including electronics, pharma, auto" },
        { q: "Digital India", a: "2015 — Digital infrastructure, e-governance, digital literacy. Key initiatives: Aadhaar, UPI, DigiLocker, CoWIN, ONDC" },
        { q: "PM-KISAN", a: "2019 — Income support of ₹6,000/year to farmer families in three equal instalments directly to bank accounts" },
        { q: "Beti Bachao Beti Padhao", a: "2015 — Survival, protection, and education of the girl child. Focus on improving Child Sex Ratio (CSR)" },
        { q: "Startup India", a: "2016 — Promote entrepreneurship: tax exemptions for 3 years, incubation support, self-certification for labour compliance" },
    ],
    amendments: [
        { q: "1st Amendment (1951)", a: "Added reasonable restrictions on FRs (Art 19), created 9th Schedule to protect land reform laws, abolished zamindari" },
        { q: "42nd Amendment (1976)", a: "'Mini Constitution' — Added Fundamental Duties (Part IVA), made India 'Socialist, Secular, Integrity' in Preamble" },
        { q: "44th Amendment (1978)", a: "Undid excesses of Emergency-era 42nd Amendment — made right to property a legal right (Art 300A)" },
        { q: "52nd Amendment (1985)", a: "Anti-Defection Law — added 10th Schedule, disqualifies members who defect from their party" },
        { q: "61st Amendment (1989)", a: "Reduced voting age from 21 to 18 years" },
        { q: "73rd Amendment (1992)", a: "Panchayati Raj — Part IX added, three-tier system, 11th Schedule (29 subjects), 33% reservation for women" },
        { q: "74th Amendment (1992)", a: "Municipalities — Part IXA added, urban local bodies, 12th Schedule (18 subjects)" },
        { q: "86th Amendment (2002)", a: "Right to Education — made education a Fundamental Right (Art 21A) for ages 6-14" },
        { q: "101st Amendment (2016)", a: "GST — Goods and Services Tax, 'one nation, one tax'. Added Art 246A for concurrent power" },
        { q: "103rd Amendment (2019)", a: "EWS Reservation — 10% reservation for Economically Weaker Sections in general category" },
    ],
    committees: [
        { q: "Sarkaria Commission (1983)", a: "Centre-State relations — recommended limited use of Art 356, governor should be non-partisan, consult CM before appointment" },
        { q: "Mandal Commission (1980)", a: "Identified OBCs using 11 indicators — recommended 27% reservation for OBCs in government jobs and education" },
        { q: "Punchhi Commission (2007)", a: "Centre-State relations — recommended fixed 5-year term for governors, impeachment procedure, state autonomy" },
        { q: "2nd ARC (2005-2009)", a: "Administrative Reforms — 15 reports on ethics, RTI, e-governance, citizen charters, disaster management" },
        { q: "Urjit Patel Committee (2014)", a: "Monetary policy framework — recommended 4% CPI inflation target with ±2% band, MPC for rate decisions" },
        { q: "Rangarajan Committee (2012)", a: "Revised poverty line estimation — recommended higher poverty lines than Tendulkar Committee" },
        { q: "Kothari Commission (1964-66)", a: "Education policy — recommended 10+2+3 pattern, national curriculum framework, Hindi as link language, 6% GDP on education" },
        { q: "Srikrishna Committee (2010)", a: "Telangana statehood — recommended against bifurcation initially, but Telangana was formed in 2014" },
    ],
    bodies: [
        { q: "Election Commission (Art 324)", a: "Independent body conducting elections to Parliament, State Legislatures, President & VP. CEC has security of tenure like SC judge" },
        { q: "UPSC (Art 315-323)", a: "Central recruiting agency — conducts CSE, IFS, IPS etc. Chairman appointed by President, removed only like SC judge" },
        { q: "Finance Commission (Art 280)", a: "Constituted every 5 years by President — recommends revenue sharing between Centre and States, grants-in-aid" },
        { q: "CAG (Art 148-151)", a: "Comptroller and Auditor General — audits all government accounts. Guardian of public purse. Removed like SC judge" },
        { q: "NCSC (Art 338)", a: "National Commission for Scheduled Castes — monitors safeguards, investigates complaints, advises on policy" },
        { q: "NCST (Art 338A)", a: "National Commission for Scheduled Tribes — monitors safeguards for STs. Created by 89th Amendment (2003)" },
        { q: "NITI Aayog (2015)", a: "Replaced Planning Commission — think tank for policy, cooperative & competitive federalism. CEO appointed by PM" },
        { q: "NHRC (1993)", a: "Statutory body (Protection of Human Rights Act, 1993) — protects and promotes human rights. Not a constitutional body" },
    ]
};

function startDeck(deckName) {
    if (deckName === 'mixed') {
        // Combine all decks
        currentDeck = [];
        Object.values(flashcardDecks).forEach(deck => {
            currentDeck = currentDeck.concat(deck);
        });
        // Shuffle
        currentDeck = shuffleArray(currentDeck);
        document.getElementById('deckTitle').textContent = '🔀 Mixed Flashcards';
    } else {
        currentDeck = [...flashcardDecks[deckName]];
        const titles = {
            articles: '📜 Constitutional Articles',
            schemes: '🏛️ Government Schemes',
            amendments: '📝 Key Amendments',
            committees: '👥 Important Committees',
            bodies: '⚖️ Constitutional Bodies'
        };
        document.getElementById('deckTitle').textContent = titles[deckName] || 'Flashcards';
    }

    currentIndex = 0;
    isFlipped = false;
    document.getElementById('flashcard').classList.remove('flipped');
    document.getElementById('flashcardSection').style.display = 'block';
    displayCard();
}

function displayCard() {
    if (!currentDeck.length) return;

    const card = currentDeck[currentIndex];
    document.getElementById('cardQuestion').textContent = card.q;
    document.getElementById('cardAnswer').textContent = card.a;
    document.getElementById('cardCounter').textContent = `${currentIndex + 1} / ${currentDeck.length}`;

    // Reset flip
    isFlipped = false;
    document.getElementById('flashcard').classList.remove('flipped');
}

function flipCard() {
    isFlipped = !isFlipped;
    document.getElementById('flashcard').classList.toggle('flipped');
}

function nextCard() {
    if (currentIndex < currentDeck.length - 1) {
        currentIndex++;
        displayCard();
    }
}

function prevCard() {
    if (currentIndex > 0) {
        currentIndex--;
        displayCard();
    }
}

function exitDeck() {
    document.getElementById('flashcardSection').style.display = 'none';
    currentDeck = [];
    currentIndex = 0;
}

function shuffleArray(array) {
    const arr = [...array];
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr;
}

// Keyboard navigation
document.addEventListener('keydown', (e) => {
    if (!currentDeck.length) return;

    if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        flipCard();
    } else if (e.key === 'ArrowRight') {
        nextCard();
    } else if (e.key === 'ArrowLeft') {
        prevCard();
    }
});
