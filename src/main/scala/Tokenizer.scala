/**
  * Created by zezzy on 9/3/16.
  */

object Tokenizer {
  def replaceLingo(word:String):Array[String] = {
    val lingo = Map("2f4u"	->"Too Fast For You", "4yeo"->"for your eyes only", "fyeo"->	"for your eyes only",
      "aamof"->"as a matter of fact", "ack"->	"acknowledgment", "afaik"->	"as far as i know", "afair"->	"	as far as i remember recall",
      "afk"->	"	away from keyboard", "aka"->	"	also known as", "b2k"->	"btk	back to keyboard", "btt"->	"back to topic", "btw"->	"	by the way",
      "b/c"->	"	because", "c&p"->	"copy and paste", "cu"->	"see you", "cys"->	"check your settings", "diy"->	"	do it yourself",
      "eobd"->	"end of business day", "eod"->	"end of discussion", "eom"->	"	end of message", "eot"->	"end of thread",
      "faq"->	"	frequently asked questions", "fack"->	"	full acknowledge", "fka"->	"formerly known as", "fwiw"->	"for what it's worth",
      "fyi"->	"for your information", "jfyi"->	"for your information", "ftw"->	"	for the win", "hf"->	"	have fun", "hth"->	"	hope this helps",
      "idk"->	"	i don't know", "iirc"->	"	if i recall correctly", "imho"->	"	in my humble opinion", "imo"->	"in my opinion",
      "imnsho"->	"	in my not so honest opinion", "iow"->	"in other words", "itt"->	"in this thread", "lol"->	"laughing out loud",
      "mmw"->	"	mark my words", "n/a"->	"	not available", "nan"->	"	not a number", "nntr"->	"	no need to reply", "noob"->	"newbie",
      "noyb"->	"none of your business", "nrn"->	"no reply necessary", "omg"->	"oh my god", "op"->	"original poster, original post",
      "ot"->	"	off topic", "otoh"->	"on the other hand", "pebkac"->	"	problem exists between keyboard and chair", "pov"->	"	point of view",
      "rotfl"->	"rolling on the floor laughing", "rtfm"->	"	read the fine manual", "scnr"->	"	sorry, could not resist", "sflr"->	"	sorry, for late reply",
      "spoc"->	"single point of contact", "tba"->	"to be announced", "tbc"->	"	to be continued", "tia"->	"	thanks in advance",
      "tgif"->	"thanks god, its friday", "thx"->	"thanks", "tnx"->"thanks", "tq"->	"thank you", "tyvm"->	"thank you very much", "tyt"->	"take your time",
      "ttyl"->	"talk to you later", "wfm"->	"works for me", "wrt"->	"with regard to", "wth"->	"what the hell", "heck"->	"hell", "2moro" -> "tomorrow",
      "wtf"->	"	what the fuck", "ymmd"->	"you made my day", "ymmv"->	"your mileage may vary", "u" -> "you", "2nte" -> "tonight"
    )

    lingo.get(word) match {
      case Some(wd) => wd.trim.split(" ")
      case _ => Array(word)
    }

  }

  def tokenize(tweet:String): List[String] = {
    //list of english language punctuations and some characters
    val punctuations = List('\'',',','-','~','"','[',']','{','}','.','(',')','!','?','*')

    val stopwords = List("a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although",
      "always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at",
      "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill",
      "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during",
      "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few",
      "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go",
      "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however",
      "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many",
      "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never",
      "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or",
      "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed",
      "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime",
      "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby",
      "therefore", "therein", "thereupon", "these", "they", "thick", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together",
      "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when",
      "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom",
      "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the")

    tweet.split(" ").flatMap(wd => replaceLingo(wd)).filter(w => !stopwords.contains(w.toLowerCase().filter(!punctuations.contains(_)))).toList

  }

}
