package com.malliina.beam

import akka.actor.ActorRef
import com.malliina.play.models.Username

class BeamClient(val user: Username, val out: ActorRef)