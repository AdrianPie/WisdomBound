package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Third batch of technology - companies, inventions and the internet not yet covered. */
object TechnologyQuestionsExtra2 {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "tec3_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Technology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("facebook_zalozyciel", "Who co-founded Facebook?", "Mark Zuckerberg", listOf("Elon Musk", "Steve Jobs", "Larry Page"), QuizDifficulty.Easy),
        ask("tesla_zalozyciel", "Who founded Tesla and SpaceX?", "Elon Musk", listOf("Jeff Bezos", "Bill Gates", "Mark Zuckerberg"), QuizDifficulty.Easy),
        ask("amazon_zalozyciel", "Who founded Amazon?", "Jeff Bezos", listOf("Elon Musk", "Bill Gates", "Larry Page"), QuizDifficulty.Easy),
        ask("microsoft_zalozyciel", "Who co-founded Microsoft?", "Bill Gates", listOf("Steve Jobs", "Jeff Bezos", "Mark Zuckerberg"), QuizDifficulty.Easy),
        ask("apple_zalozyciel", "Who co-founded Apple?", "Steve Jobs", listOf("Bill Gates", "Jeff Bezos", "Sergey Brin"), QuizDifficulty.Easy),
        ask("google_zalozyciele", "Who founded Google?", "Larry Page and Sergey Brin", listOf("Bill Gates and Paul Allen", "Steve Jobs and Steve Wozniak", "Jeff Bezos and Elon Musk"), QuizDifficulty.Easy),
        ask("usb_skrot", "What does the abbreviation USB stand for?", "Universal Serial Bus", listOf("United System Board", "Universal System Bus is a wrong variant", "Unified Serial Base"), QuizDifficulty.Easy),
        ask("pdf_skrot", "What does the abbreviation PDF stand for?", "Portable Document Format", listOf("Personal Data File", "Printed Document Format", "Public Data Format"), QuizDifficulty.Easy),
        ask("smartfon_pierwszy", "Which company released the first iPhone?", "Apple", listOf("Samsung", "Nokia", "Google"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("android_zalozenie", "Which company originally created Android, before Google bought it?", "Android Inc.", listOf("Nokia", "Motorola", "Samsung"), QuizDifficulty.Medium),
        ask("wikipedia_model", "How does Wikipedia work?", "It's built by a community of users", listOf("It's written by a single editorial team only", "It's generated entirely by AI", "It's a paid encyclopedia"), QuizDifficulty.Medium),
        ask("linux_licencja", "How is the Linux kernel distributed?", "As open-source software", listOf("As paid commercial software", "Only for businesses", "Only for military use"), QuizDifficulty.Medium),
        ask("bitcoin_tworca", "Under what pseudonym was the original Bitcoin paper published?", "Satoshi Nakamoto", listOf("John Doe", "Anonymous", "Craig Wright, indisputably"), QuizDifficulty.Medium),
        ask("email_pierwszy_rok", "In which decade was the first email sent?", "The 1970s", listOf("The 1990s", "The 1950s", "The 21st century"), QuizDifficulty.Medium),
        ask("streaming_bitrate", "What does a higher bitrate mean in video streaming?", "Usually higher image quality at the cost of more data", listOf("Noticeably lower image quality on every screen", "Pages that finish loading rather faster", "A smaller file in every single case"), QuizDifficulty.Medium),
        ask("firewall_funkcja", "What is a firewall used for?", "Controlling and filtering network traffic", listOf("Speeding up a slow internet connection", "Compressing files before they are sent", "Charging the battery more efficiently"), QuizDifficulty.Medium),
        ask("antywirus_funkcja", "What is antivirus software used for?", "Detecting and removing malware", listOf("Speeding up the computer", "Making backups", "Managing files"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("moore_prawo_tresc", "What did Moore's law predict?", "Transistor count on a chip doubling roughly every two years", listOf("Internet speeds doubling for everyone every single year", "Computer prices falling by half every year", "Drive capacity doubling every single month"), QuizDifficulty.Hard),
        ask("open_source_licencje", "Which license is one of the most well-known in open source?", "The GNU GPL", listOf("The Windows EULA", "The Adobe Creative License", "The Apple Developer Agreement"), QuizDifficulty.Hard),
        ask("machine_learning_typ", "How does supervised learning differ from unsupervised?", "Supervised data has labels, unsupervised doesn't", listOf("Supervised uses no data", "Unsupervised always needs more compute power", "They're synonyms"), QuizDifficulty.Hard),
        ask("sieci_5g", "How does 5G differ from 4G?", "Higher throughput and lower latency", listOf("It only works over cable", "It's slower but cheaper", "It requires a different alphabet"), QuizDifficulty.Hard),
        ask("edge_computing", "What is edge computing?", "Processing data close to its source instead of in a central cloud", listOf("A new kind of web browser, and nothing more besides that", "A particular family of graphics cards", "An operating system built for servers"), QuizDifficulty.Hard),
        ask("token_autoryzacja", "What is a token used for in authentication (e.g. OAuth)?", "It proves permissions without sending a password every time", listOf("It encrypts the whole of the drive it is stored on", "It keeps a backup copy of the account", "It makes the connection noticeably faster"), QuizDifficulty.Hard),
        ask("rozproszona_baza", "What sets a distributed database apart from a classic one?", "Data is stored across many connected servers", listOf("Data lives only in memory and never on disk", "It has no query language of any kind", "It works only when there is no network"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("turing_maszyna", "What is a Turing machine in computation theory?", "An abstract model of computation defining what's computable", listOf("A physical computer built in the 1940s for the army", "A program written purely to break ciphers", "A particular family of neural networks"), QuizDifficulty.Expert),
        ask("kwantowa_supremacja", "What does so-called quantum supremacy mean?", "A quantum task no classical computer could finish in reasonable time", listOf("Quantum computers have replaced every classical machine in use", "Quantum computers are now sold to the general public", "Every cipher in use in the world has been broken"), QuizDifficulty.Expert),
        ask("blockchain_konsensus", "What is the consensus mechanism Bitcoin originally used called?", "Proof of Work", listOf("Proof of Stake, first", "Proof of Authority", "Byzantine Fault Tolerance, first"), QuizDifficulty.Expert),
        ask("neural_network_backprop", "What is the key algorithm for training neural networks called?", "Backpropagation", listOf("Bubble sort", "The Fourier transform", "Dijkstra's algorithm"), QuizDifficulty.Expert),
        ask("p_np_zlozonosc", "Which complexity class do most known efficient algorithms belong to?", "P (polynomial)", listOf("NP-hard", "Uncomputable", "Always exponential"), QuizDifficulty.Expert),
        ask("gan_sieci", "What are generative adversarial networks (GANs) used for?", "Generating realistic data, like images", listOf("Lossless data compression", "Encrypting communication", "Sorting large datasets"), QuizDifficulty.Expert)
    )
}
