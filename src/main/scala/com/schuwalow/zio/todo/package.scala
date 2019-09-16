package com.schuwalow.zio

import zio._

package object todo {

  // variant with more constrained types.
  // this allows not specifying intermediate environments in main.
  implicit class ZManagedSyntax[R, E, A](zm: ZManaged[R, E, A]) {

    def >>*[E1 >: E, B](that: ZManaged[A, E1, B]): ZManaged[R, E1, B] =
      zm >>> that
  }

}
