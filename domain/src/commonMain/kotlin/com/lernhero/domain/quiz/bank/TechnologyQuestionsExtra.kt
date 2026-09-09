package com.lernhero.domain.quiz.bank

import com.lernhero.domain.quiz.QuizCategory
import com.lernhero.domain.quiz.QuizDifficulty
import com.lernhero.domain.quiz.QuizEntry

/** Second batch of technology - hardware, networks, programming and computer history. */
object TechnologyQuestionsExtra {

    private fun ask(
        id: String,
        prompt: String,
        correct: String,
        wrong: List<String>,
        difficulty: QuizDifficulty
    ) = QuizEntry(
        id = "tec2_$id",
        prompt = prompt,
        correct = correct,
        wrong = wrong,
        category = QuizCategory.Technology,
        difficulty = difficulty
    )

    val all: List<QuizEntry> = listOf(

        /* ------------------------------- Easy ------------------------------- */
        ask("ram_rola", "What is RAM used for?", "Temporarily holding data while the computer runs", listOf("Permanently storing files", "Cooling the processor", "Displaying images"), QuizDifficulty.Easy),
        ask("dysk_twardy", "What stores data after the computer is turned off?", "A drive", listOf("RAM", "The processor", "The graphics card"), QuizDifficulty.Easy),
        ask("system_windows", "Which company makes Windows?", "Microsoft", listOf("Apple", "Google", "IBM"), QuizDifficulty.Easy),
        ask("gps_rola", "What is GPS used for?", "Determining location", listOf("Encrypting data", "Charging a battery", "Compressing photos"), QuizDifficulty.Easy),
        ask("bluetooth", "What is Bluetooth used for?", "Short-range wireless connectivity", listOf("Connecting to a satellite", "Powering devices", "3D printing"), QuizDifficulty.Easy),
        ask("chmura", "What does storing files 'in the cloud' mean?", "On servers accessible over the internet", listOf("On a local drive", "In RAM", "On a CD"), QuizDifficulty.Easy),
        ask("ikona_dysk", "What does a floppy disk icon usually mean in software?", "Save", listOf("Print", "Delete", "Undo"), QuizDifficulty.Easy),

        /* ------------------------------ Medium ------------------------------ */
        ask("ip_adres", "What is an IP address?", "An identifier for a device on a network", listOf("A username", "A processor's serial number", "An encryption key"), QuizDifficulty.Medium),
        ask("dns_rola", "What is DNS used for?", "Converting domain names into IP addresses", listOf("Encrypting connections", "Compressing web pages", "Blocking ads"), QuizDifficulty.Medium),
        ask("https_co", "What does HTTPS add over HTTP?", "Encryption of the connection", listOf("Faster downloads", "Automatic translation", "Image compression"), QuizDifficulty.Medium),
        ask("gpu_rola", "What is a graphics card responsible for?", "Calculations related to images", listOf("Storing files", "Connecting to the internet", "Powering the motherboard"), QuizDifficulty.Medium),
        ask("ssd_hdd", "How does an SSD differ from a classic HDD?", "It has no moving parts and is faster", listOf("It always has more capacity", "It only works in laptops", "It needs no power"), QuizDifficulty.Medium),
        ask("linux_torvalds", "Who released the first version of the Linux kernel in 1991?", "Linus Torvalds", listOf("Richard Stallman", "Ken Thompson", "Andrew Tanenbaum"), QuizDifficulty.Medium),
        ask("kompilator", "What does a compiler do?", "Translates source code into machine code", listOf("Runs websites", "Tests hardware", "Draws the interface"), QuizDifficulty.Medium),
        ask("baza_danych", "What is the SQL language used for?", "Querying a database", listOf("Styling web pages", "3D animation", "Network configuration"), QuizDifficulty.Medium),
        ask("css_rola", "What is CSS used for?", "Describing how pages look", listOf("Application logic", "Storing data", "Network connections"), QuizDifficulty.Medium),
        ask("java_kotlin", "Which virtual machine does Kotlin run on by default?", "The JVM", listOf(".NET", "V8", "LLVM"), QuizDifficulty.Medium),
        ask("android_jezyk", "Which language does Google now recommend for writing Android apps?", "Kotlin", listOf("Swift", "Objective-C", "PHP"), QuizDifficulty.Medium),
        ask("ios_jezyk", "Which language are iOS apps written in today?", "Swift", listOf("Kotlin", "Ruby", "Go"), QuizDifficulty.Medium),
        ask("vpn", "What is a VPN used for?", "Creating a secure tunnel over a network", listOf("Speeding up the processor", "Backing up files", "Scanning drives"), QuizDifficulty.Medium),
        ask("phishing", "What is phishing?", "Stealing data by impersonating someone trustworthy", listOf("A drive failure", "Encrypting files", "A type of firewall"), QuizDifficulty.Medium),
        ask("cookies", "What are cookies?", "Small pieces of data websites store in a browser", listOf("Viruses", "System backups", "Device drivers"), QuizDifficulty.Medium),

        /* ------------------------------ Hard ------------------------------- */
        ask("git_rola", "What is Git used for?", "Code version control", listOf("Compiling programs", "Performance testing", "Managing servers"), QuizDifficulty.Hard),
        ask("api_co", "What is an API?", "An interface through which programs communicate", listOf("A programming language", "A type of database", "An encryption protocol"), QuizDifficulty.Hard),
        ask("rekurencja", "What is recursion?", "A function calling itself", listOf("An infinite loop", "An incorrect data type", "A parallel thread"), QuizDifficulty.Hard),
        ask("bigo_sort", "What is the average complexity of quicksort?", "O(n log n)", listOf("O(n)", "O(n squared)", "O(log n)"), QuizDifficulty.Hard),
        ask("hash", "What is a hash function used for?", "Computing a fixed-length signature of data", listOf("Reversible encryption", "Lossy compression", "Sorting arrays"), QuizDifficulty.Hard),
        ask("ram_vs_cache", "Which memory is the fastest?", "The processor's cache", listOf("RAM", "SSD", "A network drive"), QuizDifficulty.Hard),
        ask("ipv6", "How many bits does an IPv6 address have?", "128", listOf("32", "64", "256"), QuizDifficulty.Hard),
        ask("tcp_udp", "How does TCP differ from UDP?", "It guarantees delivery and packet order", listOf("It's always faster", "It doesn't use ports", "It only works locally"), QuizDifficulty.Hard),
        ask("kernel", "What is an operating system's kernel?", "The layer managing hardware and processes", listOf("The graphical interface", "The file manager", "A printer driver"), QuizDifficulty.Hard),
        ask("kontener", "What is Docker used for?", "Running applications in containers", listOf("Designing databases", "Writing tests", "Compressing files"), QuizDifficulty.Hard),
        ask("kompresja_stratna", "Which image format uses lossy compression?", "JPEG", listOf("PNG", "BMP", "GIF"), QuizDifficulty.Hard),
        ask("blockchain", "What is a blockchain?", "A distributed ledger of linked data blocks", listOf("A type of SQL database", "A mail protocol", "A file system"), QuizDifficulty.Hard),
        ask("sieci_neuronowe", "What is the basic unit of a neural network?", "A neuron (perceptron)", listOf("A bit", "A byte", "A register"), QuizDifficulty.Hard),

        /* ----------------------------- Expert ---------------------------- */
        ask("enigma_bomba", "What was the Polish machine for breaking Enigma called?", "The cryptologic bomb", listOf("Colossus", "Difference Engine", "Mark I"), QuizDifficulty.Expert),
        ask("colossus", "What was the British Colossus used for?", "Breaking German ciphers", listOf("Ballistics calculations", "Controlling radar", "Census counting"), QuizDifficulty.Expert),
        ask("eniac", "What was ENIAC?", "One of the first general-purpose electronic computers", listOf("The first microprocessor", "The first operating system", "The first computer network"), QuizDifficulty.Expert),
        ask("mikroprocesor_pierwszy", "What was Intel's first commercial microprocessor called?", "The 4004", listOf("The 8086", "The Z80", "The 6502"), QuizDifficulty.Expert),
        ask("unix_autorzy", "Who created the Unix operating system at Bell Labs?", "Ken Thompson and Dennis Ritchie", listOf("Bill Gates and Paul Allen", "Steve Jobs and Steve Wozniak", "Linus Torvalds"), QuizDifficulty.Expert),
        ask("www_autor", "Who invented the World Wide Web?", "Tim Berners-Lee", listOf("Vint Cerf", "Marc Andreessen", "Robert Kahn"), QuizDifficulty.Expert),
        ask("tcp_autorzy", "Who co-created the TCP/IP protocol?", "Vint Cerf and Robert Kahn", listOf("Tim Berners-Lee", "Larry Page and Sergey Brin", "Dennis Ritchie"), QuizDifficulty.Expert),
        ask("turing_test", "What does the Turing test concern?", "Telling a human apart from a machine in conversation", listOf("Processor performance", "Algorithm correctness", "Cipher security"), QuizDifficulty.Expert),
        ask("halting", "What does the halting problem state?", "There's no general way to decide whether a program will halt", listOf("Every program can be optimized", "Recursion always terminates", "Loops are always faster than recursion"), QuizDifficulty.Expert),
        ask("moore_koniec", "What increasingly limits Moore's law today?", "The physical limits of miniaturization and heat dissipation", listOf("Lack of demand for power", "Software prices", "Legal restrictions"), QuizDifficulty.Expert),
        ask("kwantowy_qubit", "How does a qubit differ from a classical bit?", "It can be in a superposition of states", listOf("It stores two bytes", "It's faster at every task", "It needs no power"), QuizDifficulty.Expert),
        ask("rsa", "What is RSA encryption's security based on?", "The difficulty of factoring large numbers", listOf("The length of the user's password", "Keeping the algorithm secret", "Processor speed"), QuizDifficulty.Expert)
    )
}
