package com.example.wordlesolver.repository

// Relative frequency percentage for each letter. Obtained from:
// https://www3.nd.edu/~busiforc/handouts/cryptography/letterfrequencies.html
val frequencyOf = mapOf(
    'e' to 11.1607f,
    'a' to 8.4966f,
    'r' to 7.5809f,
    'i' to 7.5448f,
    'o' to 7.1635f,
    't' to 6.9509f,
    'n' to 6.6544f,
    's' to 5.7351f,
    'l' to 5.4893f,
    'c' to 4.5388f,
    'u' to 3.6308f,
    'd' to 3.3844f,
    'p' to 3.1671f,
    'm' to 3.0129f,
    'h' to 3.0034f,
    'g' to 2.4705f,
    'b' to 2.0720f,
    'f' to 1.8121f,
    'y' to 1.7770f,
    'w' to 1.2899f,
    'k' to 1.1016f,
    'v' to 1.0074f,
    'x' to 0.2902f,
    'z' to 0.2722f,
    'j' to 0.1965f,
    'q' to 0.1962f
)

internal fun String.frequencyScore(): Float {
    // Get distinct letters to favor variation
    return toList().distinct().map{ char -> (frequencyOf[char])!! }.sum()
}