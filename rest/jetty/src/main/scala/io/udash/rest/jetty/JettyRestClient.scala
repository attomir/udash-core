package io.udash
package rest.jetty

import java.net.HttpCookie
import java.nio.charset.Charset

import com.avsystem.commons._
import com.avsystem.commons.annotation.explicitGenerics
import io.udash.rest.raw._
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.Result
import org.eclipse.jetty.client.util.{BufferingResponseListener, BytesContentProvider, StringContentProvider}
import org.eclipse.jetty.http.{HttpHeader, MimeTypes}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object JettyRestClient {
  final val DefaultMaxResponseLength = 2 * 1024 * 1024
  final val DefaultTimeout = 10.seconds

  @explicitGenerics def apply[RestApi: RawRest.AsRealRpc : RestMetadata](
    client: HttpClient,
    baseUri: String,
    maxResponseLength: Int = DefaultMaxResponseLength,
    timeout: Duration = DefaultTimeout
  ): RestApi =
    RawRest.fromHandleRequest[RestApi](asHandleRequest(client, baseUri, maxResponseLength, timeout))

  def asHandleRequest(
    client: HttpClient,
    baseUrl: String,
    maxResponseLength: Int = DefaultMaxResponseLength,
    timeout: Duration = DefaultTimeout
  ): RawRest.HandleRequest =
    RawRest.safeHandle { request =>
      callback =>
        val urlBuilder = new StringBuilder
        urlBuilder.append(PlainValue.encodePath(request.parameters.path))
        if (request.parameters.query.nonEmpty) {
          urlBuilder.append("?")
        }
        urlBuilder.append(RawQueryValue.encodeQuery(request.parameters.query))
        val httpReq = client.newRequest(urlBuilder.result()).method(request.method.name)

        request.parameters.headers.entries.foreach {
          case (name, PlainValue(value)) => httpReq.header(name, value)
        }
        request.parameters.cookies.entries.foreach {
          case (name, PlainValue(value)) => httpReq.cookie(new HttpCookie(name, value))
        }

        request.body match {
          case HttpBody.Empty =>
          case tb: HttpBody.Textual =>
            httpReq.content(new StringContentProvider(tb.contentType, tb.content, Charset.forName(tb.charset)))
          case bb: HttpBody.Binary =>
            httpReq.content(new BytesContentProvider(bb.contentType, bb.bytes))
        }

        timeout match {
          case fd: FiniteDuration => httpReq.timeout(fd.length, fd.unit)
          case _ =>
        }

        httpReq.send(new BufferingResponseListener(maxResponseLength) {
          override def onComplete(result: Result): Unit =
            if (result.isSucceeded) {
              val httpResp = result.getResponse
              val contentTypeOpt = httpResp.getHeaders.get(HttpHeader.CONTENT_TYPE).opt
              val charsetOpt = contentTypeOpt.map(MimeTypes.getCharsetFromContentType)
              val body = (contentTypeOpt, charsetOpt) match {
                case (Opt(contentType), Opt(charset)) =>
                  HttpBody.textual(getContentAsString, MimeTypes.getContentTypeWithoutCharset(contentType), charset)
                case (Opt(contentType), Opt.Empty) =>
                  HttpBody.binary(getContent, contentType)
                case _ =>
                  HttpBody.Empty
              }
              val headers = httpResp.getHeaders.iterator.asScala.map(h => (h.getName, PlainValue(h.getValue))).toList
              val response = RestResponse(httpResp.getStatus, IMapping(headers), body)
              callback(Success(response))
            } else {
              callback(Failure(result.getFailure))
            }
        })
    }
}
