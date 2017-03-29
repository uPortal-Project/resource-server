# Resource Server

[![Linux Build Status](https://travis-ci.org/Jasig/resource-server.svg?branch=master)](https://travis-ci.org/Jasig/resource-server)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/u60a82l41asrqpp1/branch/master?svg=true)](https://ci.appveyor.com/project/ChristianMurphy/resource-server/branch/master)

## Overview

The Resource Server is a collection of libraries and a web application geared towards more efficient inclusion of static resources such as CSS and JavaScript in Java web applications. Shared resources such as jQuery releases are included in the web application under a well defined, versioned URL with browser-side caching headers and gzip compression applied. A Maven plugin provides for JS and CSS minification and aggregation, combining many small files into a single uniquely named file to allow for browser-side caching. JSP tags allow for easy, automatic use of these aggregated files and files from the Resource Server web application when it is available and fail gracefully when it is not.

The end goal of these utilities is to reduce the number of distinct resource URLs the browser has to retrieve content from for any given page and to take advantage of browser side caching to reduce the number of requests the browser has to make.

## Using the Resource Server

The resource server is made of of four main components.

### Resource Server Web Application

The web application deployed with uPortal since the 3.1 release which provides:

1.  Well defined, versioned URLs for static resources such as JavaScript libraries.
2.  Setting aggressive browser-side caching headers for static resources.
3.  Providing server-side gzip compression of static resources.
4.  Providing a single URL for a resource that may be used by multiple web applications.
5.  Fall back gracefully to local static resources if the Resource Server is not available.
6.  Provide functionality general enough to be interesting to any Java servlet or portlet application.

### Resource Server Maven Plugin

A Maven plugin which aggregates and compresses JavaScript and CSS into a minimal set of files with unique file names to allow for aggressive browser-side caching.

### Resource Server Utilities

Utility library used by various components for common functionality

1.  Provide a utility class which does:
    *   Load lists of both aggregated and un-aggregated CSS/JS files for a web application from the resources XML file.
    *   Determine if resource aggregation is enabled or disabled based on a system property.
    *   Determine if the Resource Server Web Application is deployed and if it is if the requested resource is available from it.
2.  Provide aggregation aware JSP tags to
    *   Write out the CSS/JS link and script tags from the list of resources loaded by the utility class.
    *   Generate a resource URL based on if it is available from the Resource Server Web Application.
    *   Minify in-line JavaScript
3.  Provide aggregation aware servlet filters to
    *   GZip and cache in memory static resources
    *   Set long-term browser-side caching headers for static resources based on the path of the resource

### Resource Server Content

WAR packaged artifact that contains all of the static content available from the Resource Server Web Application. Used with Maven WAR Overlay support to include the resources used by the web application locally so that fall-back works correctly if the Resource Server Web Application is not available.
