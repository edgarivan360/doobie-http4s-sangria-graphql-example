// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats._
import cats.implicits._
import doobie._
import doobie.implicits._
import fs2.Stream
import demo.model._
import io.chrisdavenport.log4cats.Logger

trait CityRepo[F[_]] {
  def fetchById(id: Int): F[Option[City]]
  def fetchAll: Stream[F, City]
  def fetchByCountryCode(code: String): Stream[F, City]
}

object CityRepo {

  def fromTransactor[F[_]: Monad: Logger](xa: Transactor[F]): CityRepo[F] =
    new CityRepo[F] {

      val select: Fragment =
        fr"""
          SELECT id, name, countrycode, district, population
          FROM   city
        """

      def fetchById(id: Int): F[Option[City]] =
        Logger[F].info(s"CityRepo.fetchById($id)") *>
        (select ++ sql"where id = $id").query[City].option.transact(xa)

      def fetchAll: Stream[F, City] =
        Stream.eval_(Logger[F].info(s"CityRepo.fetchAll")) ++
        select.query[City].stream.transact(xa)

      def fetchByCountryCode(code: String): Stream[F, City] =
        Stream.eval_(Logger[F].info(s"CityRepo.fetchByCountryCode($code)")) ++
        (select ++ sql"where countrycode = $code").query[City].stream.transact(xa)

    }

}