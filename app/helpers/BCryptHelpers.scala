package helpers

import org.mindrot.jbcrypt.BCrypt

/** Wrapper class for BCrypt methods
  *
  */
object BCryptHelpers {

  /** Hashes a string with an auto-generated salt
    *
    * @param str - The String to be salted and hashed
    * @return The salted hash of the input string
    */
  def digest(str: String): String = {
    BCrypt.hashpw(str, BCrypt.gensalt)
  }

  /** Verifies that the candidate is equal to the prehashed value of hash
    *
    * @param candidate - The candidate being checked
    * @param hash - The salted hash being compared to
    * @return True if the candidate is correct, False otherwise
    */
  def verify(candidate: String, hash: String): Boolean = {
    BCrypt.checkpw(candidate, hash)
  }
}
