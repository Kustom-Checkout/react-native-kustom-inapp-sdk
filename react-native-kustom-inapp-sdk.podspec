require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-kustom-inapp-sdk"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platform     = :ios, "15.1"
  s.source       = { :git => "https://github.com/Kustom-Checkout/react-native-kustom-inapp-sdk.git", :tag => "v#{s.version}" }

  s.source_files = "ios/Sources/**/*.{pch,h,m,mm,swift}"
  s.requires_arc = true

  s.prefix_header_file = 'ios/Sources/PrefixHeader.pch'

  install_modules_dependencies(s)

  s.dependency 'KustomMobileSDK', '1.0.4'
end
