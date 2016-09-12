/**
  * Created by zezzy on 9/3/16.
  */
object Stemmer {

  val irregular_forms:Map[String,List[String]] =
        Map( "sky" ->  List("sky", "skies"),
        "die" ->  List("dying"),
        "lie" ->  List("lying"),
        "tie" ->  List("tying"),
        "news" ->  List("news"),
        "inning" ->  List("innings", "inning"),
        "outing" ->  List("outings", "outing"),
        "canning" ->  List("cannings", "canning"),
        "howe" ->  List("howe"),
        "proceed" ->  List("proceed"),
        "exceed"  ->  List("exceed"),
        "succeed"  ->  List("succeed")
        )
  var pool = scala.collection.mutable.Map[String, String]()
  //expand irregular_form words to map to same word
  for(key <- irregular_forms.keys){
    for(w <- irregular_forms(key)){
      pool += (w -> key)
    }
  }

  val vowels = List('a', 'e', 'i', 'o', 'u')


  /**
    * m() measures the number of consonant sequences between k0 and j.
        if c is a consonant sequence and v a vowel sequence, and <..>
        indicates arbitrary presence,
           <c><v>       gives 0
           <c>vc<v>     gives 1
           <c>vcvc<v>   gives 2
           <c>vcvcvc<v> gives 3
    * */
  private def mC(word:String,j:Int):Int ={
    var i = 0
    var n = 0
    while (true){
      if( i > j)
        return n
      if(vowels.contains(word(i)) && word.length-1 > i){
        if(!vowels.contains(word(i+1))){
          n += 1
        }
      }
      i+=1
    }
    n
  }

  /**
    * doublec(word) is TRUE <=> word ends with a double consonant
    * */
  def doublec(word:String):Boolean = {
    if (word.length < 2)
      return false
    if (word.substring(word.length-2)(0) != word.substring(word.length-2)(1) )
      return false
    return !vowels.contains(word.substring(word.length - 1))
  }



  /**
    * cvc(i) is TRUE <=>

        a) ( --NEW--) i == 1, and word[0] word[1] is vowel consonant, or

        b) word[i - 2], word[i - 1], word[i] has the form consonant -
           vowel - consonant and also if the second c is not w, x or y. this
           is used when trying to restore an e at the end of a short word.
           e.g.

               cav(e), lov(e), hop(e), crim(e), but
               snow, box, tray.
    * */
  def cvc(word:String, i:Int):Boolean = {
    if (i == 0)
      return false
    if (i ==1 )
      return vowels.contains(word(1)) && !vowels.contains(word(0))

    return !vowels.contains(word(i-2)) && vowels.contains(word(i-1)) && !vowels.contains(word(i)) && !List('x','y','z').contains(word(i))

  }

  /**
    * vowelin(stem) is TRUE <=> stem contains a vowel
    * */
  def vowelin(word:String):Boolean ={
    word.exists(vowels.contains(_))
  }

  /**
    * gets rid of plurals and -ed or -ing. e.g.
    * caresses  ->  caress
    * meetings  ->  meet
    * */
  private def step1ab(wd:String):String = {
    var word = wd
    if (word.endsWith("s")) {
      if (word.endsWith("sses"))
        word = word.substring(0, word.length - 2)
      else if (word.endsWith("ies")) {
        if (word.length == 4) {
          word = word.substring(0, word.length - 1)
        }
        //this line extends the original algorithm, so that#'flies '-> 'fli ' but 'dies '-> 'die ' etc
        else {
          word = word.substring(0, word.length - 2)
        }
      }
      else if (word.substring(word.length - 2)(0) != 's')
        word = word.substring(0, word.length - 1)
    }

    var ed_or_ing_trimmed = false
    if (word.endsWith("ied")) {
      if (word.length == 4)
        word = word.substring(0, word.length - 1)
      else
        word = word.substring(0, word.length - 2)
    }
    else if(word.endsWith("eed")){
      if(mC(word, word.length-4) > 0)
        word = word.substring(0, word.length - 1 )
    }
    else if( word.endsWith("ed") && vowelin(word.substring(0, word.length - 2))) {
      word = word.substring(0, word.length-2)
      ed_or_ing_trimmed = true
    }
    else if( word.endsWith("ing") && vowelin(word.substring(0, word.length - 3))) {
      word = word.substring(0, word.length-3)
      ed_or_ing_trimmed = true
    }

    if (ed_or_ing_trimmed){
      if (word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz"))
        word += 'e'
      else if(doublec(word)) {
        if (!List('s', 'z', 'l').contains(word(word.length - 1))) {
          word = word.substring(0, word.length - 1)
        }
      }
      else if(mC(word, word.length-1)==1 && cvc(word, word.length-1))
        word += 'e'
    }
    word
  }

  /**
    * step1c() turns terminal y to i when there is another vowel in the stem.
        --NEW--: This has been modified from the original Porter algorithm so that y->i
        is only done when y is preceded by a consonant, but not if the stem
        is only a single consonant, i.e.

           (*c and not c) Y -> I
    * */
  def step1c( word:String):String = {
    if (word(word.length-1) == 'y' && word.length > 2 && !vowels.contains(word(word.length -2)) )
      return word.substring(0, word.length-1) + 'i'
    word
  }

  /**
    * step2() maps double suffices to single ones.
        so -ization ( = -ize plus -ation) maps to -ize etc. note that the
        string before the suffix must give m() > 0
    * */
  def step2(word:String):String = {
    if(word.length <= 1)
      return word

    word(word.length - 2) match {
      case 'a' => {
        if(word.endsWith("ational")) {
          if (mC(word, word.length - 8) > 0)
            return word.substring(0, word.length - 7) + "ate"
          else
            return word
        }
        else if(word.endsWith("tional")){
          if (mC(word, word.length - 7) > 0)
            return word.substring(0, word.length - 2)
          else
            return word
        }
        else
          return word
      }
      case 'c' => {
        if(word.endsWith("enci")){
          if(mC(word, word.length-5) > 0)
            return word.substring(0, word.length - 4) + "ence"
          else
            return word
        }
        else if (word.endsWith("anci")){
          if(mC(word, word.length-5) > 0)
            return word.substring(0, word.length - 4) + "ance"
          else
            return  word
        }
        else
          return word
      }
      case 'e' => {
        if (word.endsWith("izer")){
          if(mC(word, word.length-5) > 0){
            return word.substring(0, word.length - 1)
          }else
            return word
        }else
          return  word
      }
      case 'l' => {
        if(word.endsWith("bli")){
          if(mC(word, word.length-4) > 0) {
            return word.substring(0, word.length - 3) + "ble"
          }else
            return word
        }
        else if(word.endsWith("alli")){
          if(mC(word, word.length-5) > 0) {
            return step2(word.substring(0, word.length - 2))
          }
          else
            return word
        }
        else if(word.endsWith("fulli")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 2)
          }
          else
            return word
        }
        else if(word.endsWith("entli")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 2)
          }
          else
            return word
        }
        else if(word.endsWith("ousli")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 2)
          }
          else
            return word
        }
        else if(word.endsWith("eli")){
          if(mC(word, word.length-4) > 0) {
            return word.substring(0, word.length - 2)
          }
          else
            return word
        }
        else
          return word
      }
      case 'o' => {
        if(word.endsWith("ization")){
          if(mC(word, word.length-8) > 0) {
            return word.substring(0, word.length - 7) + "ize"
          }
          else
            return word
        }
        else if(word.endsWith("ation")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 5) + "ate"
          }
          else
            return word
        }
        else if(word.endsWith("ator")){
          if(mC(word, word.length-5) > 0) {
            return word.substring(0, word.length - 4) + "ate"
          }
          else
            return word
        }
        else
          return word
      }
      case 's' => {
        if(word.endsWith("alism")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        if(word.endsWith("ness")){
          if(word.endsWith("iveness") || word.endsWith("fulness") || word.endsWith("ousness")){
            if(mC(word, word.length-8) > 0) {
              return word.substring(0, word.length - 4)
            }
            return word
          }
          return word
        }
        return word
      }
      case 't' => {
        if(word.endsWith("aliti")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        if(word.endsWith("aviti")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 5) + "ive"
          }
          return word
        }
        if(word.endsWith("biliti")){
          if(mC(word, word.length-7) > 0) {
            return word.substring(0, word.length - 6) + "ble"
          }
          return word
        }
        return word
      }
      case 'g' => {
        if(word.endsWith("logi")){
          if(mC(word, word.length-4) > 0) {//!!Departure -- use 5 to match published algorithm
            return word.substring(0, word.length - 1)
          }
          return word
        }
        return word
      }
      case _ => return word

    }
  }

/**
  * step3() deals with -ic-, -full, -ness etc. similar strategy to step2.
  * */
  def step3(word:String):String ={
  if(word.length <= 1)
    return word

  word(word.length-1) match {
      case 'e' =>{
        if(word.endsWith("icate") || word.endsWith("alize")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        if(word.endsWith("ative")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 5)
          }
          return word
        }
        return word
      }
      case 'i' => {
        if(word.endsWith("iciti")){
          if(mC(word, word.length-6) > 0) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 'l' => {
        if(word.endsWith("ical")){
          if(mC(word, word.length-5) > 0) {
            return word.substring(0, word.length - 2)
          }
          return word
        }
        if(word.endsWith("ful")){
          if(mC(word, word.length-4) > 0) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 's' => {
        if(word.endsWith("ness")){
          if(mC(word, word.length-5) > 0) {
            return word.substring(0, word.length - 4)
          }
          return word
        }
        return word
      }
      case _ => return word
    }
  }

  /**
    * step4() takes off -ant, -ence etc., in context <c>vcvc<v>
    * */
  def step4(word:String):String = {
    if(word.length <= 1)
      return word

    word(word.length-2) match {
      case 'a' => {
        if(word.endsWith("al")){
          if(mC(word, word.length-3) > 1) {
            return word.substring(0, word.length - 2)
          }
          return word
        }
        return word
      }
      case 'c' => {
        if(word.endsWith("ance") || word.endsWith("ence")){
          if(mC(word, word.length-5) > 1) {
            return word.substring(0, word.length - 4)
          }
          return word
        }
        return word
      }
      case 'e' => {
        if(word.endsWith("ance")){
          if(mC(word, word.length-3) > 1) {
            return word.substring(0, word.length - 2)
          }
          return word
        }
        return word
      }
      case 'i' => {
        if(word.endsWith("ic")){
          if(mC(word, word.length-3) > 1) {
            return word.substring(0, word.length - 2)
          }
          return word
        }
        return word
      }
      case 'l' => {
        if(word.endsWith("able") || word.endsWith("ible")){
          if(mC(word, word.length-5) > 1) {
            return word.substring(0, word.length - 4)
          }
          return word
        }
        return word
      }
      case 'n' => {
        if(word.endsWith("ant") || word.endsWith("ent")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        if(word.endsWith("ement")){
          if(mC(word, word.length-6) > 1) {
            return word.substring(0, word.length - 5)
          }
          return word
        }
        if(word.endsWith("ment")){
          if(mC(word, word.length-5) > 1) {
            return word.substring(0, word.length - 4)
          }
          return word
        }
        return word
      }
      case 'o' => {
        if(word.endsWith("sion") || word.endsWith("tion")){ //slightly differ form other logic
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        if(word.endsWith("ou")){
          if(mC(word, word.length-3) > 1) {
            return word.substring(0, word.length - 2)
          }
          return word
        }
        return word
      }
      case 's' => {
        if(word.endsWith("ism")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 't' => {
        if(word.endsWith("ate") || word.endsWith("iti")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 'u' => {
        if(word.endsWith("ous")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 'v' => {
        if(word.endsWith("ive")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case 'z' => {
        if(word.endsWith("ize")){
          if(mC(word, word.length-4) > 1) {
            return word.substring(0, word.length - 3)
          }
          return word
        }
        return word
      }
      case _ => {
        return word
      }
    }
  }


  /**
    * step5() removes a final -e if m() > 1, and changes -ll to -l if
        m() > 1
    * */
  def step5(word:String): String ={
    word(word.length-1) match {
      case 'e' => {
        if(mC(word, word.length-1) > 1 || (mC(word, word.length-1) == 1 && !cvc(word, word.length-2)) ) {
          return word.substring(0, word.length - 1)
        }
        return word
      }
      case _ => {
        if(word.endsWith("ll") && mC(word, word.length-1) > 1) {
          return word.substring(0, word.length - 1)
        }
        return word
      }
    }
  }

  def stem(wd:String ):String ={
    var word = wd
    if(pool.contains(word))
      return pool(word)

    if(word.length <= 2)
      return word

    word = step1ab(word)
    word = step1c(word)
    word = step2(word)
    word = step3(word)
    word = step4(word)
    word = step5(word)
    word
  }
}