# Kairos: Java Scheduler API

Kairos is a Java-based concurrent resource scheduler implementation for the JDK.

## Overview

> Kairos is an Ancient Greek word meaning the right, critical, or opportune moment. The ancient Greeks had two words for time: chronos and kairos. The former refers to chronological or sequential time, while the latter signifies a proper or opportune time for action. — [Kairos: Wikipedia](https://en.wikipedia.org/wiki/Kairos)

### What is Kairos?

An idea came to me a few years ago while I was looking up at the stars while listening to some electronic music.

>What if you could record a musical note every time a star died before going supernovae?

The idea somehow stuck with me, and I decided to create a particle simulation that turned supernovas into music. What could go wrong?
 
Creating a simulation to model the birth and death of stars seemed a bit ambitious to me at the time. What resulted was a scaled-down particle system that modeled the abstract idea of the birth and death of star systems.
 
When I finally got around to the music part, I had a problem, and it had to do with time.

No human will ever be able to sit around long enough to observe and record a musical note for every dying star. Even if this were the case, each musical note would be so distant in time, that no rhythm or melody could ever be recognized. This is also true when you speed things up, like in a scaled-down simulation for instance. 

To hear any music, each and every note needs to be scheduled and played sequentially (_Chronos_) while sounding at the opportune time (_Kairos_). If the notes sound too slowly or too quickly, the vibrations either blend together or not at all. Either way, without _Chronos and Kairos_, there's no way to transform data into anything resembling music.

This is why I created Kairos—a JDK-based scheduler API—which I then used to generate [musical compositions](https://soundcloud.com/kenny-bastani/unsupervised-ml-classic-piano-improv-1) based on the idea of stars making music.

_The scheduler is designed for far more than just music, which is why I wanted to open source it._

## Work-in-progress

This repository and library is a work-in-progress. I wanted to start out with a story that focuses on the "why" before adding the "how". I'll be adding some code examples and documentation as I find time. Thanks!

# License

This library is an open source product licensed under Apache License 2.0.
